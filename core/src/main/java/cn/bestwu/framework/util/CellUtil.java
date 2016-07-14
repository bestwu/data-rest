package cn.bestwu.framework.util;

/**
 * 手机号工具类
 * <p>
 * 其他号段
 * 14号段以前为上网卡专属号段，如中国联通的是145，中国移动的是147等等。
 * 170号段为虚拟运营商专属号段，170号段的 11 位手机号前四位来区分基础运营商，其中 “1700” 为中国电信的转售号码标识，“1705” 为中国移动，“1709” 为中国联通。
 * 171号段也为虚拟运营商专属号段。
 * 卫星通信 1349
 *
 * @author Peter Wu
 */
public class CellUtil {

	/**
	 * 中国移动号段
	 * 2G号段（GSM网络）有134x（0-8）、135、136、137、138、139、150、151、152、158、159、182、183、184。
	 * 3G号段（TD-SCDMA网络）有157、187、188
	 * 3G上网卡 147
	 * 4G号段 178、184
	 */
	private static final String CHINA_MOBILE_CELL_REGEX = "^(134\\d|135\\d|136\\d|137\\d|138\\d|139\\d|150\\d|151\\d|152\\d|157\\d|158\\d|159\\d|182\\d|183\\d|184\\d|187\\d|178\\d|188\\d|147\\d|1705)\\d{7}$";
	/**
	 * 中国联通号段
	 * 2G号段（GSM网络）130、131、132、155、156
	 * 3G上网卡145
	 * 3G号段（WCDMA网络）185、186
	 * 4G号段 176、185[1]
	 */
	private static final String CHINA_UNICOM_CELL_REGEX = "^(130\\d|131\\d|132\\d|155\\d|156\\d|176\\d|185\\d|186\\d|1709)\\d{7}$";
	/**
	 * 中国电信号段
	 * 2G/3G号段（CDMA2000网络）133、153、180、181、189
	 * 4G号段 177、173
	 */
	private static final String CHINA_TELECOM_CELL_REGEX = "^(133\\d|153\\d|173\\d|177\\d|180\\d|181\\d|189\\d|1700)\\d{7}$";
	/**
	 * 中国虚拟运营商号段
	 */
	private static final String CHINA_VNO_CELL_REGEX = "^(170|171)\\d{8}$";

	/**
	 * 是否为中国移动号码
	 *
	 * @param cell 手机号码
	 * @return 是否为中国移动号码
	 */
	public static boolean isChinaMobile(String cell) {
		if (cell == null) {
			return false;
		} else {
			return cell.matches(CHINA_MOBILE_CELL_REGEX);
		}
	}

	/**
	 * 是否为中国联通号码
	 *
	 * @param cell 手机号码
	 * @return 是否为中国联通号码
	 */
	public static boolean isChinaUnicom(String cell) {
		if (cell == null) {
			return false;
		} else {
			return cell.matches(CHINA_UNICOM_CELL_REGEX);
		}
	}

	/**
	 * 是否为中国电信号码
	 *
	 * @param cell 手机号码
	 * @return 是否为中国电信号码
	 */
	public static boolean isChinaTelecom(String cell) {
		if (cell == null) {
			return false;
		} else {
			return cell.matches(CHINA_TELECOM_CELL_REGEX);
		}
	}

	/**
	 * 是否为中国虚拟运营商号码
	 *
	 * @param cell 手机号码
	 * @return 是否为中国虚拟运营商号码
	 */
	public static boolean isChinaVNO(String cell) {
		if (cell == null) {
			return false;
		} else {
			return cell.matches(CHINA_VNO_CELL_REGEX);
		}
	}

	/**
	 * 是否为中国大陆手机号
	 *
	 * @param cell 手机号码
	 * @return 是否为中国大陆手机号
	 */
	public static boolean isChinaCell(String cell) {
		if (cell == null) {
			return false;
		} else {
			return isChinaMobile(cell) || isChinaUnicom(cell) || isChinaTelecom(cell) || isChinaVNO(cell);
		}
	}
}
