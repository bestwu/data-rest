package cn.bestwu.framework.util.keyword.filter;

import cn.bestwu.framework.util.keyword.CharNode;
import cn.bestwu.framework.util.keyword.MatchType;

import java.util.*;

/**
 * 可忽略中间的特殊字符,比如：过*滤，中的*
 *
 * @author Peter Wu
 */
public class SkipKeywordFilter extends SimpleKeywordFilter {
	private final Set<Character> skipChars = new HashSet<>(0);
	private boolean skip = false;

	@Override
	public String replace(String text) {
		if (skip) {
			CharNode last = root;
			StringBuilder result = new StringBuilder();
			char[] words = text.toCharArray();
			boolean matchShort = matchType.equals(MatchType.SHORT);
			List<Integer> ignoredWords = new ArrayList<>();
			for (int i = 0; i < words.length; i++) {
				char word = words[i];

				int length = last.getLength();
				length = length + ignoredWords.size();
				int lastIndex = i - length;
				boolean skipChar = skipChars.contains(word);
				boolean end = i == words.length - 1;
				boolean containLast = false;
				if (!skipChar) {
					CharNode charNode = last.get(word);
					if (charNode != null) {
						last = charNode;
						length++;
						containLast = true;
					}
				} else if (!end) {
					ignoredWords.add(i);
					continue;
				}
				boolean lastEnd = last.isEnd();
				if (last == root) {
					for (Integer integer : ignoredWords) {
						result.append(words[integer]);
					}
					result.append(word);
					ignoredWords.clear();
				} else if (containLast && matchShort && lastEnd) {
					result.append(strategy
							.replaceWith(Arrays.copyOfRange(words, lastIndex, lastIndex + length)));
					ignoredWords.clear();
					last = root;
				} else if (!containLast || end) {
					if (lastEnd) {
						result.append(strategy.replaceWith(Arrays.copyOfRange(words, lastIndex,
								lastIndex + length)));
						if (!containLast) {
							i--;
						}
					} else {
						// 未结束，找短匹配
						if (matchShort) {
							i = lastIndex;
							result.append(words[i]);
						} else {
							CharNode failNode = last.getFailNode();
							if (failNode == root) {
								i = lastIndex;
								result.append(words[i]);
							} else {
								int failLength = failNode.getLength();
								i = lastIndex + failLength - 1;

								int count = 0;
								for (Integer integer : ignoredWords) {
									if (integer > i) {
										break;
									}
									count++;
								}
								failLength = failLength + count;
								result.append(strategy.replaceWith(Arrays.copyOfRange(words,
										lastIndex, lastIndex + failLength)));
							}
						}
					}
					last = root;
					ignoredWords.clear();
				}
			}
			return result.toString();
		} else {
			return super.replace(text);
		}
	}

	public void addSkipChar(Collection<Character> chars) {
		if (null != chars && !chars.isEmpty()) {
			this.skipChars.addAll(chars);
			this.skip = true;
		}
	}

}