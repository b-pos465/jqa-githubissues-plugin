package jdom;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class XMLParser {

    /**
     * Parses the plugin configuration file.
     *
     * @param inputStream The InputStream that shall be used.
     * @return A list of repository configurations.
     * @throws JDOMException If XML parsing failed.
     * @throws IOException   If reading the config file failed.
     */
    public static List<XMLGitHubRepository> parseConfiguration(InputStream inputStream) throws JDOMException, IOException {

        List<XMLGitHubRepository> repositoryList = new ArrayList<>();

        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(inputStream);
        for (Element repository : document.getRootElement().getChildren("github-repository")) {
            repositoryList.add(new XMLGitHubRepository(
                    repository.getChildText("user"),
                    repository.getChildText("name"),
                    new XMLCredentials(
                            repository.getChild("credentials").getChildText("user"),
                            repository.getChild("credentials").getChildText("password")
                    )));
        }

        return repositoryList;
    }
}