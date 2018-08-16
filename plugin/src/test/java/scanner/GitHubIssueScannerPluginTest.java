package scanner;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import model.GitHub;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import toolbox.RestTool;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.support.SuppressCode.suppressConstructor;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestTool.class)
public class GitHubIssueScannerPluginTest extends AbstractPluginIT {

//    @Mock
//    RestTool restToolMock = mock(RestTool.class);


    @Test
    public void scanGitHubIssues() {

        suppressConstructor(RestTool.class);
        mockStatic(RestTool.class);
        RestTool restToolMock = mock(RestTool.class);

        when(RestTool.getInstance()).thenReturn(restToolMock);

//        when(restToolMock.requestIssuesByRepository(Mockito.anyObject(), Mockito.anyObject())).thenReturn("test");

        store.beginTransaction();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("githubissues.xml")).getFile());

        Descriptor descriptor = getScanner().scan(file, "/githubissues.xml", DefaultScope.NONE);

        assertThat(descriptor, CoreMatchers.instanceOf(GitHub.class));

        store.commitTransaction();
    }
}
