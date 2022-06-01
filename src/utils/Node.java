package utils;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import classes.FileStructure;
 
/**
 * 
 * @author w w w. j a v a g i s t s . c o m
 *
 */
public class Node<T> implements Serializable {
 
	private static final long serialVersionUID = 2950544014445577948L;
	private static final Logger LOGGER = LogManager.getLogger(Node.class);
	private T data = null; 
	private List<Node<T>> children = new ArrayList<>(); 
	private Node<T> parent = null;

	// Nach Konvention wenn hier == true <=> DIRECTORY 
	private Boolean specialOne = null;
	
	// Nach Konvention wenn hier == true <=> FILE 
	private Boolean specialTwo = null;
	
	
	public Node(T data) {
		this.data = data;
	}
	
	public Node<T> getRoot() {
		if(parent == null){
			return this;
		}
		return parent.getRoot();
	}
 
	public Node<T> addChild(Node<T> child) {
		child.setParent(this);
		this.children.add(child);
		return child;
	}
 
	public void addChildren(List<Node<T>> children) {
		children.forEach(each -> each.setParent(this));
			this.children.addAll(children);
	}
 
	public List<Node<T>> getChildren() {
		return children;
	}
 
	public T getData() {
		return data;
	}
 
	public void setData(T data) {
		this.data = data;
	}
 
	private void setParent(Node<T> parent) {
		this.parent = parent;
	}
 
	public Node<T> getParent() {
		return parent;
	}
	
	public Boolean getSpecialOne() {
		return specialOne;
	}

	public void setSpecialOne(Boolean specialOne) {
		this.specialOne = specialOne;
	}

	public Boolean getSpecialTwo() {
		return specialTwo;
	}

	public void setSpecialTwo(Boolean specialTwo) {
		this.specialTwo = specialTwo;
	}
	
	@Override
	public String toString() {
		return this.getData().toString();
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}		
	
	public static <T> void printTree(Node<T> node, String appender) {
	   System.out.println(appender + node.getData());
	   node.getChildren().forEach(each ->  printTree(each, appender + appender));
	}
 
}
