package com.yuncore.maiziedu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
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
	public static void main(String[] args) {

		if (args == null || args.length < 2) {
			System.err.println("Uage: courseurl savepath");
		} else {
			
			List<CourseBean> courseBeans = null;
			
			while (true) {
				try {
					courseBeans = parseCourseHtml(args[0]);
					break;
				} catch (Exception e1) {
					System.err.println("parseCourseHtml error");
				}
			}
			
			if(null == courseBeans || courseBeans.isEmpty()){
				return ;
			}
			
			for (CourseBean c :courseBeans) {
				String path = args[1] + File.separator + c.getName();
				new File(path).mkdirs();
				
				 List<EduBean> eduBeans = null;
				while (true) {
					try {
						eduBeans = parseHtml(c.getUrl());
						break;
					} catch (Exception e1) {
						System.err.println("parseHtml error");
					}
				}
				
				if(null == eduBeans || eduBeans.isEmpty()){
					continue ;
				}
				
				for (EduBean str : eduBeans) {

					while (true) {

						try {
							final String videoUrl = getVideo("http://www.maiziedu.com"
									+ str.getUrl());
							if (null != videoUrl) {

								downloadVideo(videoUrl, path, str.getName()
										+ ".mp4");
								break;

							} else {
								System.err.println("not found video file");
								System.exit(0);
							}
						} catch (Exception e) {
							System.out.println(String
									.format("[ downloadVideo error"));
						}
					}
				}
			}
		}

	}

	public static List<CourseBean> parseCourseHtml(String htmlfile)
			throws IOException, ParserException {
		System.out
				.println(String.format("[ parseHtml htmlfile:%s ]", htmlfile));
		String courese_regx = new URL(htmlfile).getPath();
		courese_regx += "/[\\w|-]*";
		final HttpURLConnection openConnection = (HttpURLConnection) new URL(htmlfile).openConnection();
		openConnection.setConnectTimeout(10000);
		openConnection.setReadTimeout(10000);
		Parser parser = new Parser(openConnection);
		parser.setEncoding("UTF-8");
		TagNameFilter filter = new TagNameFilter("a");
		NodeList list = parser.parse(filter);

		String href = null;
		List<CourseBean> beans = new ArrayList<CourseBean>();
		CourseBean bean = null;
		for (int i = 0; i < list.size(); i++) {
			LinkTag tag = (LinkTag) list.elementAt(i);
			href = tag.getAttribute("href");
			// System.out.println(href);
			if (href != null && href.matches(courese_regx)) {
				System.out.print("[ " + tag.getAttribute("href") + "    ");
				System.out.println(tag.getAttribute("title") + " ]");
				bean = new CourseBean();
				bean.setUrl("http://www.maiziedu.com" + href);
				bean.setName(tag.getAttribute("title").trim());
				beans.add(bean);
			}
		}
		System.out.println(String.format("[ find download file :%s ]",
				beans.size()));
		return beans;
	}

	public static List<EduBean> parseHtml(String htmlfile) throws IOException,
			ParserException {
		System.out
				.println(String.format("[ parseHtml htmlfile:%s ]", htmlfile));
		String courese_regx = new URL(htmlfile).getPath();
		courese_regx = courese_regx.substring(0, courese_regx.lastIndexOf("/"));
		courese_regx += "/[\\w|-]*/";
		final HttpURLConnection openConnection = (HttpURLConnection) new URL(htmlfile).openConnection();
		openConnection.setConnectTimeout(10000);
		openConnection.setReadTimeout(10000);
		Parser parser = new Parser(openConnection);
		parser.setEncoding("UTF-8");
		TagNameFilter filter = new TagNameFilter("a");
		NodeList list = parser.parse(filter);

		String href = null;
		List<EduBean> beans = new ArrayList<EduBean>();
		EduBean bean = null;
		for (int i = 0; i < list.size(); i++) {
			LinkTag tag = (LinkTag) list.elementAt(i);
			href = tag.getAttribute("href");
			// System.out.println(href);
			if (href != null && href.matches(courese_regx)) {
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
		System.out.println(String.format("[ find download file :%s ]",
				beans.size()));
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
		Pattern pattern = Pattern
				.compile("http://(\\w|\\.)*.maiziedu.com/(\\w|\\.|%|\\-|\\(|\\))*.mp4");
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

		openConnection.setConnectTimeout(10000);
		openConnection.setReadTimeout(10000);
		openConnection.setDoInput(true);
		openConnection.setDoOutput(false);

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

		openConnection.setConnectTimeout(10000);
		openConnection.setReadTimeout(10000);
		openConnection.setDoInput(true);
		openConnection.setDoOutput(false);

		int contentlength = openConnection.getContentLength();
		if(contentlength <=0){
			throw new IOException("contentlength " + contentlength);
		}
		System.out.println(String.format("[ downloadVideo contentlength:%s ]",
				bytes2kb(contentlength)));
		FileOutputStream out = null;
		final File file = new File(prefix + "/" + filename);
		if (!file.exists() || file.length() != contentlength) {
			file.getParentFile().mkdirs();
			out = new FileOutputStream(file);
			InputStream inputStream = openConnection.getInputStream();

			final byte[] bs = new byte[1024 * 1024];
			int len = 0;
			int count = 0;
			int mscount = 0;
			long msstart = System.currentTimeMillis();
			while (-1 != (len = inputStream.read(bs))) {
				out.write(bs, 0, len);
				count += len;
				mscount += len;
				if (System.currentTimeMillis() - msstart >= 1000) {
					System.out.println(String.format(
							"[ downloadVideo length: %s/%s speed:%s ]",
							bytes2kb(count), bytes2kb(contentlength),
							bytes2kb(mscount)));
					mscount = 0;
					msstart = System.currentTimeMillis();
				}

			}

			System.out.println(String.format(
					"[ downloadVideo length: %s/%s speed:%s ]", bytes2kb(count),
					bytes2kb(contentlength), bytes2kb(mscount)));
			out.flush();
			out.close();
			inputStream.close();
		}

		System.out.println(String.format("[ downloadVideo finish ]"));

		openConnection.disconnect();
		return true;

	}

	/**
	 * byte(字节)根据长度转成kb(千字节)和mb(兆字节)
	 * 
	 * @param bytes
	 * @return
	 */
	public static String bytes2kb(long bytes) {
		BigDecimal filesize = new BigDecimal(bytes);
		BigDecimal megabyte = new BigDecimal(1024 * 1024);
		float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
				.floatValue();
		if (returnValue > 1)
			return (returnValue + "MB");
		BigDecimal kilobyte = new BigDecimal(1024);
		returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
				.floatValue();
		return (returnValue + "KB");
	}

}
