package toolbox;

import com.buschmais.jqassistant.core.store.api.Store;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.jersey.api.client.UniformInterfaceException;
import jdom.XMLGitHubRepository;
import json.JSONUser;
import model.GitHubMarkdownPointer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class MarkdownParser {

    private static final String[] REPLACES = {"https://github.com/", "commit/", "issues/"};

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownParser.class);

    public static void getReferencesInMarkdown(
            Store store,
            String markdown,
            GitHubMarkdownPointer gitHubMarkdownPointer,
            XMLGitHubRepository xmlGitHubRepository) throws IOException {

        try {
            String html = RestTool.getInstance().requestMarkdownToHtml(markdown, xmlGitHubRepository);

            Document htmlDocument = Jsoup.parse(html);

            for (Element issueElement : htmlDocument.select("a.issue-link")) {

                List<String> hrefCut = removeDispensableHrefPartsAndCutIt(issueElement.attr("href"));

                gitHubMarkdownPointer.getGitHubIssues().add(
                        StoreTool.findOrCreateGitHubIssue(
                                store,
                                hrefCut.get(0),
                                hrefCut.get(1),
                                hrefCut.get(2),
                                xmlGitHubRepository));
            }

            for (Element commitElement : htmlDocument.select("a.commit-link")) {

                List<String> hrefCut = removeDispensableHrefPartsAndCutIt(commitElement.attr("href"));

                gitHubMarkdownPointer.getGitHubCommits().add(
                        StoreTool.findOrCreateGitHubCommit(store,
                                hrefCut.get(0),
                                hrefCut.get(1),
                                hrefCut.get(2)));
            }

            for (Element userElement : htmlDocument.select("a.user-mention")) {

                List<String> hrefCut = removeDispensableHrefPartsAndCutIt(userElement.attr("href"));

                gitHubMarkdownPointer.getGitHubUsers().add(
                        StoreTool.findOrCreateGitHubUser(store, new JSONUser(hrefCut.get(0))));
            }


        } catch (JsonProcessingException | UniformInterfaceException e) {
            LOGGER.error("Converting markdown to html threw an error:", e);
        }
    }

    private static List<String> removeDispensableHrefPartsAndCutIt(String href) {

        List<String> result = new ArrayList<>();

        for (String replace : REPLACES) {
            href = href.replaceAll(replace, "");
        }

        // A user annotation consists of only one element: The user name.
        if (!href.contains("/")) {
            result.add(href);
        } else {
            // Issue and commit references are tuple (repoUser, repoName, commitSha | issueNumber).

            // Get repository user:
            result.add(href.substring(0, href.indexOf("/")));
            href = href.substring(href.indexOf("/") + 1);

            // Get repository name:
            result.add(href.substring(0, href.indexOf("/")));
            href = href.substring(href.indexOf("/") + 1);

            // Get commit sha or issue number:
            result.add(href);
        }

        return result;
    }
}