package cn.bestwu.framework.jpa.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 测试用
 *
 * @author Peter Wu
 */
@Entity
public class TestUser4 implements Serializable {

	private static final long serialVersionUID = 873688637874451303L;
	@Id
	@GeneratedValue
	private Long id;

	//	@NotBlank
	private String firstName;
	//	@NotBlank
	private String lastName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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
