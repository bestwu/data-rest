package cn.bestwu.framework.util.keyword.filter;

import cn.bestwu.framework.util.keyword.CharNode;
import cn.bestwu.framework.util.keyword.MatchType;
import cn.bestwu.framework.util.keyword.replace.ReplaceStrategy;
import cn.bestwu.framework.util.keyword.replace.DefaultReplaceStrategy;

import java.util.Arrays;
import java.util.Collection;

/**
 * 简单实现
 *
 * @author Peter Wu
 */
public class SimpleKeywordFilter implements KeywordFilter {
	protected final CharNode root = new CharNode();
	protected MatchType matchType = MatchType.LONG;
	protected ReplaceStrategy strategy = new DefaultReplaceStrategy();

	@Override
	public String replace(String text) {
		CharNode last = root;
		StringBuilder result = new StringBuilder();
		char[] words = text.toCharArray();
		boolean matchShort = matchType.equals(MatchType.SHORT);
		for (int i = 0; i < words.length; i++) {
			char word = words[i];

			int length = last.getLength();
			int lastIndex = i - length;
			boolean end = i == words.length - 1;
			boolean containLast = false;
			CharNode charNode = last.get(word);
			if (charNode != null) {
				last = charNode;
				length++;
				containLast = true;
			}
			boolean lastEnd = last.isEnd();
			if (last == root) {
				result.append(word);
			} else if (containLast && matchShort && lastEnd) {
				result.append(strategy.replaceWith(Arrays.copyOfRange(words, lastIndex, lastIndex + length)));
				last = root;
			} else if (!containLast || end) {
				if (lastEnd) {
					result.append(strategy
							.replaceWith(Arrays.copyOfRange(words, lastIndex, lastIndex + length)));
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
							result.append(strategy.replaceWith(Arrays.copyOfRange(words,
									lastIndex, lastIndex + failLength)));
						}
					}
				}
				last = root;
			}
		}
		return result.toString();
	}

	@Override
	public void compile(Collection<String> keywords) {
		addKeywords(keywords);
		// 构建失败节点
		buildFailNode(root);
	}

	/**
	 * 构建char树，作为搜索的数据结构。
	 *
	 * @param keywords 关键字
	 */
	protected void addKeywords(Collection<String> keywords) {
		// 加入关键字字符串
		for (String keyword : keywords) {
			if (null == keyword || keyword.trim().isEmpty()) {
				throw new IllegalArgumentException("过滤关键词不能为空！");
			}
			char[] charArray = keyword.toCharArray();
			CharNode node = root;
			for (char aCharArray : charArray) {
				node = node.addChild(aCharArray);
			}
			node.addChild(null);
		}
	}

	/**
	 * 构建失败节点
	 *
	 * @param node 节点
	 */
	protected void buildFailNode(CharNode node) {
		doFailNode(node);
		Collection<CharNode> childNodes = node.childNodes();
		childNodes.forEach(this::buildFailNode);
	}

	private void doFailNode(CharNode node) {
		if (node == root) {
			return;
		}
		CharNode parent = node.getParent();

		while (!parent.isEnd() && parent != root) {
			parent = parent.getParent();
		}
		node.setFailNode(parent);
	}

	/**
	 * 设置匹配模式
	 *
	 * @param matchType matchType
	 */
	public void setMatchType(MatchType matchType) {
		this.matchType = matchType;
	}

	/**
	 * 设置替换策略
	 *
	 * @param strategy 替换策略
	 */
	public void setStrategy(ReplaceStrategy strategy) {
		this.strategy = strategy;
	}

}
