package cn.bestwu.framework.data;

import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 地理位置，经纬度
 */
@Embeddable
public class Location {
	/**
	 * 纬度
	 */
	@JsonView(Object.class)
	@Column(columnDefinition = "double(9,6)")
	//	@Column(scale = 9, precision = 6)
	private Double lat;
	/**
	 * 经度
	 */
	@JsonView(Object.class)
	@Column(columnDefinition = "double(9,6)")
	private Double lng;
	//--------------------------------------------

	public Location() {
	}

	public Location(Double lng, Double lat) {
		this.lat = lat;
		this.lng = lng;
	}

	//--------------------------------------------
	public Double getLng() {
		return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}
	//--------------------------------------------

	/**
	 * 纬度
	 *
	 * @return 弧度
	 */
	public double latRadians() {
		return Math.toRadians(lat);
	}

	/**
	 * 经度 弧度
	 *
	 * @return 弧度
	 */
	public double lngRadians() {
		return Math.toRadians(lng);
	}

	/**
	 * @return 坐标是否完整
	 */
	public boolean isComplete() {
		return lat != null && lng != null;
	}

	/**
	 * 单位：米
	 */
	private static final int EARTH_RADIUS = 6378137;// 地球半径

	/**
	 * @param other 其他坐标
	 * @return 米
	 */
	public int getDistance(Location other) {

		double otherLatRadians = other.latRadians(), otherLngRadians = other.lngRadians(),
				meLatRadians = latRadians(), meLngRadians = lngRadians();
		double a = otherLatRadians - meLatRadians;
		double b = otherLngRadians - meLngRadians;

		Double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(otherLatRadians) * Math.cos(meLatRadians) * Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		return s.intValue();
	}

	//--------------------------------------------
	@Override
	public String toString() {
		return lng + "," + lat;
	}

}