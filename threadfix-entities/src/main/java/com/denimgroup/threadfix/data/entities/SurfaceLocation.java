////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2014 Denim Group, Ltd.
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.denimgroup.threadfix.views.AllViews;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonView;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "SurfaceLocation")
public class SurfaceLocation extends BaseEntity {

	public static final int HOST_LENGTH = 255;
	public static final int PARAMETER_LENGTH = 255;
	public static final int PATH_LENGTH = 255;
	public static final int QUERY_LENGTH = 255;
	
	public static final Set<String> REQUEST_METHODS;

	static {
		String[] args = {"POST", "GET", "DELETE", "OPTIONS",
                "PUT", "HEAD", "TRACE"};

        REQUEST_METHODS = new HashSet<String>(Arrays.asList(args));
	}

	private static final long serialVersionUID = -8999892961251231213L;
	private final static SanitizedLogger LOGGER = new SanitizedLogger(SurfaceLocation.class);

	private Finding finding;
	
	@Size(max = HOST_LENGTH, message = "{errors.maxlength}")
	private String host;
	
	@Size(max = PARAMETER_LENGTH, message = "{errors.maxlength}")
	private String parameter;

	@Size(max = PATH_LENGTH, message = "{errors.maxlength}")
	private String path;
	
	private int port;
	
	@Size(max = 15, message = "{errors.maxlength}")
	private String protocol;
	
	@Size(max = 15, message = "{errors.maxlength}")
	private String httpMethod;
	
	@Size(max = QUERY_LENGTH, message = "{errors.maxlength}")
	private String query;
	private URL url;

	@OneToOne(mappedBy = "surfaceLocation")
	@JsonIgnore
	public Finding getFinding() {
		return finding;
	}

	public void setFinding(Finding finding) {
		this.finding = finding;
	}

	@Column(length = HOST_LENGTH)
	@JsonIgnore
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Column(length = PARAMETER_LENGTH)
    @JsonView(AllViews.TableRow.class)
	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	@Column(length = PATH_LENGTH)
    @JsonView(AllViews.TableRow.class)
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Basic
	@JsonIgnore
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Column(length = 15)
	@JsonIgnore
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	@Column(length = 15)
	public String getHttpMethod() {
		return httpMethod;
	}
	
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	@Column(length = QUERY_LENGTH)
	@JsonIgnore
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * @return A url from the base parts stored in the database or, if a
	 *         previous URL has been set, a reference to that URL.
	 * @throws java.net.MalformedURLException
	 *             Thrown if a the parts of a URL stored in the database are
	 *             invalid.
	 */
	@Transient
    @JsonIgnore
	public URL getUrl() {
		if (url == null) {
			try {
				int tempPort = -1;
				// 0 is the default (and the field can't be null), but -1 is the
				// default for the URL
				// constructor
				if (port != 0)
					tempPort = port;

				if ((protocol != null) && (host != null) && (tempPort != -1) && (path != null)) {
					url = new URL(protocol, host, tempPort, path + '?' + query);
				} else if (path != null && host != null) {

                    String HOST_PATTERN = "http://([a-zA-Z0-9_.]*)";
                    String tempHost = getRegexResult(host, HOST_PATTERN);
                    tempHost = (tempHost != null && !tempHost.isEmpty()) ? tempHost : host;
					url = new URL("http", tempHost, tempPort, path);
				} else if (path != null) {
					url = new URL("http", "localhost", tempPort, path);
				} else {
					return null;
				}
			} catch (MalformedURLException e) {
				LOGGER.error("Bad Surface Location URL with ID = " + this.getId(), e);
				throw new IllegalArgumentException(e);
			}
		}
		
		return url;
	}


    private String getRegexResult(String targetString, String regex) {
        if (targetString == null || targetString.isEmpty() || regex == null || regex.isEmpty()) {
            LOGGER.warn("getRegexResult got null or empty input.");
            return null;
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(targetString);

        return matcher.find() ? matcher.group(1) : null;
    }

	/**
	 * Sets the url's constituent parts for db stored. Stores a reference to the
	 * URL which will be returned by the getUrl call.
	 * 
	 * @param url
	 *            The URL object to store.
	 */
	public void setUrl(URL url) {
		if (url == null) return;
		this.url = url;
		this.host = url.getHost();
		this.path = url.getPath();
		this.port = url.getPort();
		this.protocol = url.getProtocol();
		this.query = url.getQuery();
	}
}
