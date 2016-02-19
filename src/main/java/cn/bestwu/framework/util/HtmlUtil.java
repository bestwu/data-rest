package cn.bestwu.framework.util;

import org.apache.lucene.analysis.CharFilter;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;

import java.io.IOException;
import java.io.StringReader;

public class HtmlUtil {

	public static String subParserHtml(String inputString, int length) {
		if (inputString == null) {
			return null;
		}
		String subHtml = parserHtmlRemoveBlank(inputString);
		return StringUtil.subString(subHtml, length);// 返回文本字符串
	}

	public static String parserHtml(String inputString) {
		if (inputString == null) {
			return null;
		}
		try {
			StringBuilder sb = new StringBuilder();
			// html过滤
			HTMLStripCharFilter htmlscript = new HTMLStripCharFilter(new StringReader(inputString));
			char[] buffer = new char[10240];
			int count;
			while ((count = htmlscript.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, count));
			}
			htmlscript.close();
			return sb.toString();
		} catch (IOException e) {
			return inputString;
		}
	}

	public static String parserHtmlRemoveBlank(String inputString) {
		if (inputString == null) {
			return null;
		}
		try {
			StringBuilder sb = new StringBuilder();
			// html过滤
			HTMLStripCharFilter htmlscript = new HTMLStripCharFilter(new StringReader(inputString));

			//增加映射过滤  主要过滤掉换行符
			NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
			builder.add("\r", "");//回车
			builder.add("\t", "");//横向跳格
			builder.add("\n", "");//换行
			builder.add(" ", "");//空白
			CharFilter cs = new MappingCharFilter(builder.build(), htmlscript);

			char[] buffer = new char[10240];
			int count;
			while ((count = cs.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, count));
			}
			cs.close();
			return sb.toString();
		} catch (IOException e) {
			return inputString;
		}
	}
}
