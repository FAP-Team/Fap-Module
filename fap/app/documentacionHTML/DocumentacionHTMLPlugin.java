package documentacionHTML;

import play.Play;
import play.PlayPlugin;
import play.libs.IO;
import play.libs.MimeTypes;
import play.mvc.Http;
import play.mvc.Router;
import play.mvc.results.Result;
import play.vfs.VirtualFile;

import java.io.File;

public class DocumentacionHTMLPlugin extends PlayPlugin {


    @Override
    public void onRoutesLoaded() {
        Router.addRoute("GET", "/@documentation/html/{id}", "fap.DocumentacionHTML.index");
    }


}


