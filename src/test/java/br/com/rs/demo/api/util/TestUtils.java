package br.com.rs.demo.api.util;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.util.ResourceUtils;

public class TestUtils {

	public static String buildURL(int port, final String uri) {
		return "http://localhost:" + port + uri;
	}

	public static String jsonFromFile(String path) throws IOException {
		return new String(Files.readAllBytes(ResourceUtils.getFile(path).toPath()));
	}
	
	public static String extractURIByRel(final String linkHeader, final String rel) {
	    if (linkHeader == null) {
	        return null;
	    }

	    String uriWithSpecifiedRel = null;
	    final String[] links = linkHeader.split(",<");
	    String linkRelation = null;
	    for (String link : links) {
	        final int positionOfSeparator = link.indexOf(';');
	        linkRelation = link.substring(positionOfSeparator + 1, link.length()).trim();
	        if (extractTypeOfRelation(linkRelation).equals(rel)) {
	            uriWithSpecifiedRel = link.substring(1, positionOfSeparator - 1);
	            break;
	        }
	    }

	    return uriWithSpecifiedRel;
	}

	static Object extractTypeOfRelation(final String linkRelation) {
	    final int positionOfEquals = linkRelation.indexOf('=');
	    return linkRelation.substring(positionOfEquals + 2, linkRelation.length() - 1).trim();
	}
}
