package controllers.fap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import play.Play;
import play.libs.IO;
import play.mvc.Controller;
import play.vfs.VirtualFile;

public class AppAPI extends Controller {

	public static void index() throws Exception {
        page("index.html");
    }

    public static void page(String id) throws Exception {
        File page = new File(Play.applicationPath, "javadoc/"+id);
        if(!page.exists()) {
            notFound("API page for "+id+" not found");
        }
        String textile = IO.readContentAsString(page);

        renderHtml(textile);
    }
}
