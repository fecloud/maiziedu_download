package com.yuncore.maiziedu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * Maiziedu.java
 * 2015-3-10
 * 深圳市五月高球信息咨询有限公司
 * 欧阳丰
 */

/**
 * 
 */
public class Maiziedu {

	/**
	 * @param args
	 * @throws IOException
	 * @throws ParserException
	 */
	public static void main(String[] args) throws IOException, ParserException {

		if (args == null || args.length < 2) {
			System.err.println("Uage: htmlfile savepath");
		} else {
			for (EduBean str : parseHtml(args[0])) {
				final String videoUrl = getVideo("http://www.maiziedu.com"
						+ str.getUrl());
				if (null != videoUrl) {
					while(true){
						try{
							downloadVideo(videoUrl, args[1], str.getName() + ".mp4");
							break;
						}catch (Exception e) {
							System.out.println(String.format("[ downloadVideo %s error", videoUrl));
						}
					}
				}
			}
		}

	}

	public static List<EduBean> parseHtml(String htmlfile) throws IOException,
			ParserException {
		System.out.println(String.format("[ parseHtml htmlfile:%s ]", htmlfile));
		Parser parser = new Parser(htmlfile);
		parser.setEncoding("UTF-8");
		TagNameFilter filter = new TagNameFilter("a");
		NodeList list = parser.parse(filter);

		String href = null;
		List<EduBean> beans = new ArrayList<EduBean>();
		EduBean bean = null;
		for (int i = 0; i < list.size(); i++) {
			LinkTag tag = (LinkTag) list.elementAt(i);
			href = tag.getAttribute("href");
			if (href != null && href.matches("/lesson/\\w*/")) {
				System.out.print("[ " + tag.getAttribute("href") + "    ");
				System.out.println(tag.toPlainTextString().replaceAll("&nbsp;",
						"")
						+ " ]");
				bean = new EduBean();
				bean.setUrl(href);
				bean.setName(tag.toPlainTextString().replaceAll("&nbsp;", ""));
				beans.add(bean);
			}
		}
		System.out.println(String.format("[ find download file :%s ]", beans.size()));
		return beans;
	}

	public static List<EduBean> parseUL() throws IOException {
		final String name = "maiziedu";
		InputStream resourceAsStream = new FileInputStream(name);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				resourceAsStream));
		String line = null;
		List<EduBean> beans = new ArrayList<EduBean>();
		EduBean bean = null;
		while (null != (line = reader.readLine())) {
			line = line.trim();
			bean = new EduBean();
			bean.setUrl(line.split("=")[0]);
			bean.setName(line.split("=")[1]);
			beans.add(bean);
		}

		resourceAsStream.close();
		return beans;
	}

	public static String getVideo(String address) throws IOException {
		final String str = getHtml(address);
		Pattern pattern = Pattern.compile("http://(\\w|\\.)*.maiziedu.com/(\\w|\\.)*.mp4");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find())
			return matcher.group();
		return null;
	}

	public static String getHtml(String address) throws IOException {
		System.out.println(String.format("[ getHtml address:%s ]", address));
		final URL url = new URL(address);
		final HttpURLConnection openConnection = (HttpURLConnection) url
				.openConnection();

		openConnection.setDoInput(true);
		openConnection.setDoOutput(true);

		InputStream inputStream = openConnection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		String line = null;
		StringBuilder buffer = new StringBuilder();
		while (null != (line = reader.readLine())) {

			buffer.append(line).append("\n");
		}

		inputStream.close();
		openConnection.disconnect();
		return buffer.toString();
	}

	public static boolean downloadVideo(String url, String prefix,
			String filename) throws IOException {
		System.out.println(String.format(
				"[ downloadVideo prefix:%s  url:%s filename:%s ]", url, prefix,
				filename));
		final URL address = new URL(url);
		final HttpURLConnection openConnection = (HttpURLConnection) address
				.openConnection();

		openConnection.setDoInput(true);
		openConnection.setDoOutput(true);

		int contentlength = openConnection.getContentLength();
		System.out.println(String.format("[ downloadVideo contentlength:%s ]",
				contentlength));
		FileOutputStream out = null;
		final File file = new File(prefix + "/" + filename);
		if (!file.exists() || file.length() != contentlength) {
			file.getParentFile().mkdirs();
			out = new FileOutputStream(file);
			InputStream inputStream = openConnection.getInputStream();

			final byte[] bs = new byte[1024 * 1024 * 2];
			int len = 0;
			int count = 0;
			while (-1 != (len = inputStream.read(bs))) {
				out.write(bs, 0, len);
				count += len;
				System.out.println(String.format("[ downloadVideo length: %s/%s ]",
						count,contentlength));
			}
			out.flush();
			out.close();
			inputStream.close();
		}
		
		System.out.println(String.format("[ downloadVideo finish ]"));

		openConnection.disconnect();
		return true;

	}

}
