package cn.bestwu.framework.rest.support;

/**
 * 版本号与serializationView
 * @author Peter Wu
 */
public class VersionedSerializationView implements Comparable<VersionedSerializationView> {

	private final String version;

	private final Class<?> serializationView;

	public VersionedSerializationView(String version, Class<?> serializationView) {
		this.version = version;
		this.serializationView = serializationView;
	}

	public String getVersion() {
		return version;
	}

	public Class<?> getSerializationView() {
		return serializationView;
	}

	@Override public String toString() {
		return serializationView.getSimpleName();
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof VersionedSerializationView))
			return false;

		VersionedSerializationView that = (VersionedSerializationView) o;

		if (!version.equals(that.version))
			return false;
		return serializationView.equals(that.serializationView);

	}

	@Override public int hashCode() {
		int result = version.hashCode();
		result = 31 * result + serializationView.hashCode();
		return result;
	}

	@Override public int compareTo(VersionedSerializationView other) {
		return Version.compareVersion(String.valueOf(version), String.valueOf(other.getVersion()));
	}
}
