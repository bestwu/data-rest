package cn.bestwu.framework.mongodb.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 测试用
 *
 * @author Peter Wu
 */
@Document
public class TestUser implements Serializable {

	private static final long serialVersionUID = -4452630632601963240L;
	@Id
	//	@GeneratedValue
	private String id;

	//	@NotBlank
	@TextIndexed
	private String firstName;
	//	@NotBlank
	@TextIndexed
	private String lastName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override public String toString() {
		return "TestUser{" +
				"id=" + id +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				'}';
	}
}

