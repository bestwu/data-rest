package cn.bestwu.framework.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Peter Wu
 */
public class CellUtilTest {

	@Test
	public void isChinaMobile() throws Exception {
		Assert.assertEquals(true, CellUtil.isChinaMobile("18224060120"));
		Assert.assertEquals(false, CellUtil.isChinaMobile("18681276622"));
		Assert.assertEquals(false, CellUtil.isChinaMobile("18000581926"));
		Assert.assertEquals(false, CellUtil.isChinaMobile("18130029925"));
	}

	@Test
	public void isChinaUnicom() throws Exception {
		Assert.assertEquals(false, CellUtil.isChinaUnicom("18224060120"));
		Assert.assertEquals(true, CellUtil.isChinaUnicom("18681276622"));
		Assert.assertEquals(false, CellUtil.isChinaUnicom("18000581926"));
		Assert.assertEquals(false, CellUtil.isChinaUnicom("18130029925"));
	}

	@Test
	public void isChinaTelecom() throws Exception {
		Assert.assertEquals(false, CellUtil.isChinaTelecom("18224060120"));
		Assert.assertEquals(false, CellUtil.isChinaTelecom("18681276622"));
		Assert.assertEquals(true, CellUtil.isChinaTelecom("18000581926"));
		Assert.assertEquals(true, CellUtil.isChinaTelecom("18130029925"));
	}

	@Test
	public void isVNO() throws Exception {
		Assert.assertEquals(false, CellUtil.isChinaVNO("18224060120"));
		Assert.assertEquals(false, CellUtil.isChinaVNO("18681276622"));
		Assert.assertEquals(false, CellUtil.isChinaVNO("18000581926"));
		Assert.assertEquals(false, CellUtil.isChinaVNO("18130029925"));
		Assert.assertEquals(true, CellUtil.isChinaVNO("17030029925"));
		Assert.assertEquals(true, CellUtil.isChinaVNO("17130029925"));
	}

	@Test
	public void isChinaCell() throws Exception {
		Assert.assertEquals(true, CellUtil.isChinaCell("18224060120"));
		Assert.assertEquals(true, CellUtil.isChinaCell("18681276622"));
		Assert.assertEquals(true, CellUtil.isChinaCell("18000581926"));
		Assert.assertEquals(true, CellUtil.isChinaCell("18130029925"));
		Assert.assertEquals(true, CellUtil.isChinaCell("17030029925"));
		Assert.assertEquals(true, CellUtil.isChinaCell("17130029925"));
	}
}
