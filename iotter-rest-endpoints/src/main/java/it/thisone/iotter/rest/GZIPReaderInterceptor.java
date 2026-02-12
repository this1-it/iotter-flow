package it.thisone.iotter.rest;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

/*
 * https://jersey.github.io/documentation/latest/user-guide.html
 */
@Provider
public class GZIPReaderInterceptor implements ReaderInterceptor {
	private HttpHeaders httpHeaders;

	public GZIPReaderInterceptor(@Context @NotNull HttpHeaders httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

	@Override
	public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
		MultivaluedMap<String, String> requestHeaders = httpHeaders.getRequestHeaders();
		List<String> values = requestHeaders.get(HttpHeaders.CONTENT_ENCODING);

		if (values != null && values.contains("gzip")) {
			try {
				final InputStream originalInputStream = context.getInputStream();
				context.setInputStream(new GZIPInputStream(originalInputStream));
			} catch (EOFException e) {

			}
		}

		return context.proceed();
	}
}