package cn.bestwu.framework.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

/**
 * 基本数据模型，其他的模型继承自此模型
 *
 * @author Peter Wu
 */
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class Model {

	/**
	 * 模型id
	 */
	@Id
	@GeneratedValue
	@JsonView(Object.class)
	private Long id;

	@Version
	@Column(columnDefinition = "bigint(20) DEFAULT '0'")
	private long version = 0;
	/**
	 * 创建时间 毫秒数
	 */
	@CreatedDate
	private Long createdTime;
	/**
	 * 修改时间 毫秒数
	 */
	@LastModifiedDate
	private Long lastModifiedTime;
	/**
	 * 资源被删除的时间 毫秒数
	 * <p>
	 * 假删除时使用
	 */
	private Long deletedTime;

	/**
	 * 资源状态描述
	 * 0:删除
	 * 1:正常
	 */
	@Column(columnDefinition = "tinyint(1) DEFAULT '1'")
	private byte status = 1;

	//------------------------------------------
	@Transient
	@JsonIgnore
	private String action;

	//--------------------------------------------
	public Model() {
	}

	public Model(Long id) {
		this.id = id;
	}

	//--------------------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Long createdTime) {
		this.createdTime = createdTime;
	}

	public Long getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setLastModifiedTime(Long lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	public Long getDeletedTime() {
		return deletedTime;
	}

	public void setDeletedTime(Long deletedTime) {
		this.deletedTime = deletedTime;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

	//------------------------------------------

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
