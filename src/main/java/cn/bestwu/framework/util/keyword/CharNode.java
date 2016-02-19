package cn.bestwu.framework.util.keyword;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 每个节点的值隐含在父节点children Map的key上，根节点为一个无值空节点
 */
public class CharNode implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 父节点
	 */
	private CharNode parent;
	/**
	 * 子节点
	 */
	private Map<Character, CharNode> children = new HashMap<>(
			0);

	/**
	 * 匹配失败时，指向较短的匹配，如：‘我是谁’，匹配失败时，指向，‘我是’节点
	 */
	private CharNode failNode;

	/**
	 * 字符所在层级，即匹配的字符串的长度;
	 */
	private int length;

	public CharNode() {
		super();
	}

	public CharNode(CharNode parent, int length) {
		super();
		this.parent = parent;
		this.length = length;
	}

	public CharNode getParent() {
		return parent;
	}

	public void setParent(CharNode parent) {
		this.parent = parent;
	}

	public Map<Character, CharNode> getChildren() {
		return children;
	}

	public void setChildren(Map<Character, CharNode> children) {
		this.children = children;
	}

	public CharNode getFailNode() {
		return failNode;
	}

	public void setFailNode(CharNode failNode) {
		this.failNode = failNode;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	// function
	public CharNode addChild(Character character) {
		CharNode charNode = children.get(character);
		if (charNode == null) {
			int length = this.length + 1;
			charNode = new CharNode(this, length);
			children.put(character, charNode);
		}

		return charNode;
	}

	// public CharNode addChild(Character character) {
	// CharNode charNode = children.get(character);
	// if (charNode == null) {
	// int length = this.length + 1;
	// charNode = new CharNode(this, length);
	// children.put(character, charNode);
	// }
	//
	// return charNode;
	// }

	public Set<Character> keys() {
		return children.keySet();
	}

	public Collection<CharNode> childNodes() {
		return children.values();
	}

	public CharNode get(char c) {
		return children.get(c);
	}

	public boolean isEnd() {
		CharNode charNode = children.get(null);
		return charNode != null;
	}

	// public char[] getWords() {
	// StringBuilder words = new StringBuilder();
	// words.append(ch);
	// CharNode parent = this.parent;
	// while (parent != null && parent != root) {
	// words.append(parent.getCh());
	// parent = parent.getParent();
	// }
	// words = words.reverse();
	// return words.toString().toCharArray();
	// }
}
