package com.euromoby.rest.handler.mail;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.mail.MailManager;
import com.euromoby.mail.model.MailAccount;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.IOUtils;

@Component
public class MailListHandler extends RestHandlerBase {

	public static final String URL = "/mail";

	private MailManager mailManager;
	
	@Autowired
	public MailListHandler(MailManager mailManager) {
		this.mailManager = mailManager;
	}	
	
	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}
	
	@Override
	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) {
		InputStream is = MailListHandler.class.getResourceAsStream("maillist.html");
		String pageContent = IOUtils.streamToString(is);

		List<MailAccount> accounts = mailManager.getAccounts();
		StringBuffer sb = new StringBuffer();

		for (MailAccount account : accounts) {
			sb.append("<tr>");
			sb.append("<td>").append(account.getLogin()).append("@").append(account.getDomain()).append("</td>");
			sb.append("<td>").append(account.getActive()).append("</td>");
			sb.append("<td><a href=\"/mail/box/");
			sb.append(account.getDomain());
			sb.append("/");
			sb.append(account.getLogin());
			sb.append("\">Inbox</a> | <a href=\"/mail/edit/");
			sb.append(account.getDomain());
			sb.append("/");
			sb.append(account.getLogin());			
			sb.append("\">Edit</a></td>");				
			sb.append("</tr>");
		}
		pageContent = pageContent.replace("%MAIL_LIST%", sb.toString());

		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}

}
