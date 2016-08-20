package cn.bestwu.framework.jpa.model;

import cn.bestwu.framework.data.annotation.DisableSelfRel;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 测试用
 *
 * @author Peter Wu
 */
@Indexed
@Entity
@DisableSelfRel
public class TestUser implements Serializable{

	private static final long serialVersionUID = -5307014753508599553L;
	@Id
	@GeneratedValue
	private Long id;

	@Field
	//	@NotBlank
	private String firstName;
	//	@NotBlank
	@Field
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

