package openhack.team12;

import openhack.team12.pages.Page;
import fi.iki.elonen.NanoHTTPD;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import openhack.team12.rest.Rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class WebServer extends NanoHTTPD {
	public static final long START_TIME = System.currentTimeMillis();

	private static final String WEB_CONTENT_DIR = "web-content";

	private Configuration m_cfg;

	public static void main(String[] args) throws IOException {
		new WebServer();
	}

	public WebServer() throws IOException {
		super(8080);
		m_cfg = new Configuration(Configuration.VERSION_2_3_26);
		m_cfg.setDirectoryForTemplateLoading(new File(WEB_CONTENT_DIR + "/template"));
		m_cfg.setDefaultEncoding("UTF-8");
		m_cfg.setLocale(Locale.US);
		m_cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		System.out.println("WebServer is running");
	}

	@Override
	public Response serve(IHTTPSession session) {
		// Try and serve a page
		Response response = Page.servePage(session, m_cfg);
		if (response != null) {
			return response;
		}

		// Try and serve a REST response
		response = Rest.process(session);
		if (response != null) {
			return response;
		}

		String uri = session.getUri();
		InputStream fileStream = getInputStream(uri);
		if (fileStream == null) {
			return Page.serveError(session, 404);
		}
		String mimeType = null;
		if (uri.endsWith(".js")) {
			mimeType = "application/javascript";
		} else if (uri.endsWith(".css")) {
			mimeType = "text/css";
		} else if (uri.endsWith(".html")) {
			mimeType = "text/html";
		} else if (uri.endsWith(".ico")) {
			mimeType = "image/x-icon";
		}
		if (mimeType == null) {
			return Page.serveError(session, 404);
		}
		return newChunkedResponse(Response.Status.OK, mimeType, fileStream);
	}

	private InputStream getInputStream(String uri) {
		try {
			return new FileInputStream(WEB_CONTENT_DIR + uri);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
