package github.forvisual.aac.aac_admin_app;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class AacContextTest {

	@Test
	public void test_before이미지_읽기() {
		AppContext ctx = AppContext.getInstance();
		List<WorkImage> images = ctx.getBeforeImages();
		for (WorkImage img : images) {
			System.out.println(img);
		}
	}
	
	@Test
	public void test_pick_max() {
		List<String> names = Arrays.asList("567g.png", "123.png", "432.gif", "213.jpg");
		AppContext ctx = AppContext.getInstance();
		assertEquals("432.gif", ctx.pickMax(names));
	}
	
	@Test
	public void test_max_filename() {
		AppContext ctx = AppContext.getInstance();
		System.out.println(ctx.getMaxPictureNum());
	}

}