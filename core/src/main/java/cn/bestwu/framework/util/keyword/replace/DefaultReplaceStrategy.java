package cn.bestwu.framework.util.keyword.replace;

import java.util.Arrays;

/**
 * 默认替换策略，匹配的字符替换为“*”
 */
public class DefaultReplaceStrategy implements ReplaceStrategy {

	@Override
	public char[] replaceWith(char[] words) {
		Arrays.fill(words, '*');
		return words;
	}

}
