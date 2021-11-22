package com.epam.deltix.qsrv.jetty;

import io.jooby.*;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.WriteListener;
import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpHeaderValue;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.MultiMap;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.util.*;
import java.util.concurrent.Executor;

import static org.eclipse.jetty.http.HttpHeader.CONTENT_TYPE;
import static org.eclipse.jetty.http.HttpHeader.SET_COOKIE;

public class JettyContext implements DefaultContext {
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);
    private final int bufferSize;
    private final long maxRequestSize;
    Request request;
    Response response;
    private QueryString query;
    private Formdata form;
    private Multipart multipart;
    private List<FileUpload> files;
    private ValueNode headers;
    private Map<String, String> pathMap = Collections.EMPTY_MAP;
    private Map<String, Object> attributes = new HashMap<>();
    private Router router;
    private Route route;
    private MediaType responseType;
    private Map<String, String> cookies;
    private HashMap<String, String> responseCookies;
    private boolean responseStarted;
    private Boolean resetHeadersOnError;
    private String method;
    private String requestPath;
    private CompletionListeners listeners;
    private String remoteAddress;
    private String host;
    private String scheme;
    private int port;

    public JettyContext(Request request, Router router, int bufferSize, long maxRequestSize) {
        this.request = request;
        this.response = request.getResponse();
        this.router = router;
        this.bufferSize = bufferSize;
        this.maxRequestSize = maxRequestSize;
        this.method = request.getMethod().toUpperCase();
        this.requestPath = request.getRequestURI();
    }

    @Nonnull
    @Override public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override public @Nonnull Map<String, String> cookieMap() {
        if (this.cookies == null) {
            this.cookies = Collections.emptyMap();
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                this.cookies = new LinkedHashMap<>(cookies.length);
                for (jakarta.servlet.http.Cookie it : cookies) {
                    this.cookies.put(it.getName(), it.getValue());
                }
            }
        }
        return cookies;
    }

    @Nonnull @Override public Body body() {
        try {
            InputStream in = request.getInputStream();
            long len = request.getContentLengthLong();
            return Body.of(this, in, len);
        } catch (IOException x) {
            throw SneakyThrows.propagate(x);
        }
    }

    @Nonnull @Override public Router getRouter() {
        return router;
    }

    @Nonnull @Override public String getMethod() {
        return method;
    }

    @Nonnull @Override public Context setMethod(@Nonnull String method) {
        this.method = method.toUpperCase();
        return this;
    }

    @Nonnull @Override public Route getRoute() {
        return route;
    }

    @Nonnull @Override public Context setRoute(Route route) {
        this.route = route;
        return this;
    }

    @Nonnull @Override public String getRequestPath() {
        return requestPath;
    }

    @Nonnull @Override public Context setRequestPath(@Nonnull String path) {
        this.requestPath = path;
        return this;
    }

    @Nonnull @Override public Map<String, String> pathMap() {
        return pathMap;
    }

    @Nonnull @Override public Context setPathMap(Map<String, String> pathMap) {
        this.pathMap = pathMap;
        return this;
    }

    @Nonnull @Override public QueryString query() {
        if (query == null) {
            query = QueryString.create(this, request.getQueryString());
        }
        return query;
    }

    @Nonnull @Override public Formdata form() {
        if (form == null) {
            form = Formdata.create(this);
            formParam(request, form);
        }
        return form;
    }

    @Nonnull @Override public Multipart multipart() {
        throw new NotImplementedException("Not implemented");
    }

    @Nonnull @Override public ValueNode header() {
        if (headers == null) {
            Enumeration<String> names = request.getHeaderNames();
            Map<String, Collection<String>> headerMap = new LinkedHashMap<>();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                headerMap.put(name, Collections.list(request.getHeaders(name)));
            }
            headers = Value.headers(this, headerMap);
        }
        return headers;
    }

    @Nonnull @Override public String getHost() {
        return host == null ? DefaultContext.super.getHost() : host;
    }

    @Nonnull @Override public Context setHost(@Nonnull String host) {
        this.host = host;
        return this;
    }

    @Override public int getPort() {
        return port > 0 ? port : DefaultContext.super.getPort();
    }

    @Nonnull @Override public Context setPort(int port) {
        this.port = port;
        return this;
    }

    @Nonnull @Override public String getRemoteAddress() {
        if (remoteAddress == null) {
            String remoteAddr = Optional.ofNullable(request.getRemoteAddr())
                    .orElse("")
                    .trim();
            return remoteAddr;
        }
        return remoteAddress;
    }

    @Nonnull @Override public Context setRemoteAddress(@Nonnull String remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }

    @Nonnull @Override public String getProtocol() {
        return request.getProtocol();
    }

    @Nonnull @Override public List<Certificate> getClientCertificates() {
        return Arrays.asList((Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate"));
    }

    @Nonnull @Override public String getScheme() {
        if (scheme == null) {
            scheme = request.isSecure() ? "https" : "http";
        }
        return scheme;
    }

    @Nonnull @Override public Context setScheme(@Nonnull String scheme) {
        this.scheme = scheme;
        return this;
    }

    @Override public boolean isInIoThread() {
        return false;
    }

    @Nonnull @Override public Context dispatch(@Nonnull Runnable action) {
        return dispatch(router.getWorker(), action);
    }

    @Nonnull @Override
    public Context dispatch(@Nonnull Executor executor, @Nonnull Runnable action) {
        if (router.getWorker() == executor) {
            action.run();
        } else {
            ifStartAsync();
            executor.execute(action);
        }
        return this;
    }

    @Nonnull @Override public Context detach(@Nonnull Route.Handler next) throws Exception {
        ifStartAsync();
        next.apply(this);
        return this;
    }

    @Nonnull @Override public Context upgrade(@Nonnull WebSocket.Initializer handler) {
        throw new NotImplementedException("Not implemented");
    }

    @Nonnull @Override public Context upgrade(@Nonnull ServerSentEmitter.Handler handler) {
        throw new NotImplementedException("Not implemented");
    }

    @Nonnull @Override public StatusCode getResponseCode() {
        return StatusCode.valueOf(response.getStatus());
    }

    @Nonnull @Override public Context setResponseCode(int statusCode) {
        response.setStatus(statusCode);
        return this;
    }

    @Nonnull @Override public MediaType getResponseType() {
        return responseType == null ? MediaType.text : responseType;
    }

    @Nonnull @Override public Context setDefaultResponseType(@Nonnull MediaType contentType) {
        if (responseType == null) {
            setResponseType(contentType, contentType.getCharset());
        }
        return this;
    }

    @Nonnull @Override
    public Context setResponseType(@Nonnull MediaType contentType, @Nullable Charset charset) {
        this.responseType = contentType;
        response.setHeader(CONTENT_TYPE, contentType.toContentTypeHeader(charset));
        return this;
    }

    @Nonnull @Override public Context setResponseType(@Nonnull String contentType) {
        this.responseType = MediaType.valueOf(contentType);
        response.setHeader(CONTENT_TYPE, contentType);
        return this;
    }

    @Nonnull @Override public Context setResponseHeader(@Nonnull String name, @Nonnull String value) {
        response.setHeader(name, value);
        return this;
    }

    @Nonnull @Override public Context removeResponseHeader(@Nonnull String name) {
        response.setHeader(name, null);
        return this;
    }

    @Nonnull @Override public Context removeResponseHeaders() {
        response.reset();
        return this;
    }

    @Nullable @Override public String getResponseHeader(@Nonnull String name) {
        return response.getHeader(name);
    }

    @Nonnull @Override public Context setResponseLength(long length) {
        response.setContentLengthLong(length);
        return this;
    }

    @Override public long getResponseLength() {
        long len = response.getContentLength();
        if (len == -1) {
            String lenStr = response.getHeader(HttpHeader.CONTENT_LENGTH.asString());
            if (lenStr != null) {
                len = Long.parseLong(lenStr);
            }
        }
        return len;
    }

    @Nonnull public Context setResponseCookie(@Nonnull Cookie cookie) {
        if (responseCookies == null) {
            responseCookies = new HashMap<>();
        }
        cookie.setPath(cookie.getPath(getContextPath()));
        responseCookies.put(cookie.getName(), cookie.toCookieString());
        response.setHeader(SET_COOKIE, null);
        for (String cookieString : responseCookies.values()) {
            response.addHeader(SET_COOKIE.asString(), cookieString);
        }
        return this;
    }

    @Nonnull @Override public Sender responseSender() {
        throw new NotImplementedException("not implemented");
    }

    @Nonnull @Override public OutputStream responseStream() {
        responseStarted = true;
        try {
            ifSetChunked();
            return response.getOutputStream();
        } catch (IOException x) {
            throw SneakyThrows.propagate(x);
        }
    }

    @Nonnull @Override public PrintWriter responseWriter(MediaType type, Charset charset) {
        setResponseType(type, charset);
        return new PrintWriter(responseStream());
    }

    @Nonnull @Override public Context send(StatusCode statusCode) {
        response.setStatus(statusCode.value());
        send(EMPTY_BUFFER);
        return this;
    }

    @Nonnull @Override public Context send(@Nonnull ByteBuffer[] data) {
        if (response.getContentLength() <= 0) {
            setResponseLength(BufferUtil.remaining(data));
        }
        ifStartAsync();
        HttpOutput out = response.getHttpOutput();
        out.setWriteListener(writeListener(request.getAsyncContext(), out, data));
        responseStarted = true;
        return this;
    }

    @Nonnull @Override public Context send(@Nonnull byte[] data) {
        return send(ByteBuffer.wrap(data));
    }

    @Nonnull @Override public Context send(@Nonnull String data, @Nonnull Charset charset) {
        return send(ByteBuffer.wrap(data.getBytes(charset)));
    }

    @Nonnull @Override public Context send(@Nonnull ByteBuffer data) {
        try {
            if (response.getContentLength() == -1) {
                response.setContentLengthLong(data.remaining());
            }
            responseStarted = true;
            HttpOutput sender = response.getHttpOutput();
            sender.sendContent(data);
            return this;
        } catch (IOException x) {
            throw SneakyThrows.propagate(x);
        } finally {
            responseDone();
        }
    }

    @Nonnull @Override public Context send(@Nonnull ReadableByteChannel channel) {
        responseStarted = true;
        ifSetChunked();
        return sendStreamInternal(Channels.newInputStream(channel));
    }

    @Nonnull @Override public Context send(@Nonnull InputStream in) {
        try {
            if (in instanceof FileInputStream) {
                response.setLongContentLength(((FileInputStream) in).getChannel().size());
            }
            return sendStreamInternal(in);
        } catch (IOException x) {
            throw SneakyThrows.propagate(x);
        }
    }

    private Context sendStreamInternal(@Nonnull InputStream in) {
        try {
            long len = response.getContentLength();
            InputStream stream;
            if (len > 0) {
                stream = ByteRange.parse(request.getHeader(HttpHeader.RANGE.asString()), len)
                        .apply(this)
                        .apply(in);
            } else {
                response.setHeader(HttpHeader.TRANSFER_ENCODING, HttpHeaderValue.CHUNKED.asString());
                stream = in;
            }
            responseStarted = true;
            response.getHttpOutput().sendContent(stream);
            return this;
        } catch (IOException x) {
            throw SneakyThrows.propagate(x);
        } finally {
            responseDone();
        }
    }

    @Nonnull @Override public Context send(@Nonnull FileChannel file) {
        try (FileChannel channel = file) {
            response.setLongContentLength(channel.size());
            return sendStreamInternal(Channels.newInputStream(file));
        } catch (IOException x) {
            throw SneakyThrows.propagate(x);
        } finally {
            responseDone();
        }
    }

    @Override public boolean isResponseStarted() {
        return responseStarted;
    }

    @Override public boolean getResetHeadersOnError() {
        return resetHeadersOnError == null
                ? getRouter().getRouterOptions().contains(RouterOption.RESET_HEADERS_ON_ERROR)
                : resetHeadersOnError.booleanValue();
    }

    @Override public Context setResetHeadersOnError(boolean resetHeadersOnError) {
        this.resetHeadersOnError = resetHeadersOnError;
        return this;
    }

    @Nonnull @Override public Context onComplete(@Nonnull Route.Complete task) {
        if (listeners == null) {
            listeners = new CompletionListeners();
        }
        listeners.addListener(task);
        return this;
    }

    @Override public String toString() {
        return getMethod() + " " + getRequestPath();
    }

    void complete(Throwable x) {
        try {
            Logger log = router.getLog();
            if (x != null) {
                if (Server.connectionLost(x)) {
                    log.debug("exception found while sending response {} {}", getMethod(), getRequestPath(),
                            x);
                } else {
                    log.error("exception found while sending response {} {}", getMethod(), getRequestPath(),
                            x);
                }
            }
        } finally {
            responseDone();
        }
    }

    private void clearFiles() {
        if (files != null) {
            for (FileUpload file : files) {
                try {
                    file.destroy();
                } catch (Exception e) {
                    router.getLog().debug("file upload destroy resulted in exception", e);
                }
            }
            files.clear();
            files = null;
        }
    }

    void responseDone() {
        try {
            ifSaveSession();

            clearFiles();
        } finally {
            if (request.isAsyncStarted()) {
                request.getAsyncContext().complete();
            }
            if (listeners != null) {
                listeners.run(this);
            }
        }
    }

    private void ifSaveSession() {
        Session session = (Session) getAttributes().get(Session.NAME);
        if (session != null && (session.isNew() || session.isModify())) {
            SessionStore store = router.getSessionStore();
            store.saveSession(this, session);
        }
    }

    private void ifStartAsync() {
        if (!request.isAsyncStarted()) {
            request.startAsync();
        }
    }

    private void ifSetChunked() {
        if (response.getContentLength() <= 0) {
            response.setHeader(HttpHeader.TRANSFER_ENCODING, HttpHeaderValue.CHUNKED.asString());
        }
    }

    private FileUpload register(FileUpload upload) {
        if (files == null) {
            files = new ArrayList<>();
        }
        files.add(upload);
        return upload;
    }

    private static void formParam(Request request, Formdata form) {
        Enumeration<String> names = request.getParameterNames();
        MultiMap<String> query = request.getQueryParameters();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (query == null || !query.containsKey(name)) {
                String[] values = request.getParameterValues(name);
                if (values != null) {
                    for (String value : values) {
                        form.put(name, value);
                    }
                }
            }
        }
    }

    private static WriteListener writeListener(AsyncContext async, HttpOutput out,
                                               ByteBuffer[] data) {
        return new WriteListener() {
            int i = 0;

            @Override public void onWritePossible() throws IOException {
                while (out.isReady()) {
                    if (i < data.length) {
                        out.write(data[i++]);
                    } else {
                        async.complete();
                        return;
                    }
                }
            }

            @Override public void onError(Throwable x) {
                async.complete();
            }
        };
    }


}