package cna.util;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

import org.jfree.chart.JFreeChart;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;

/**
 * Provides functionallity to create SVG graphics for one or multiple JFreeChart objects
 * @author Stefan Grabuschnig
 *
 */
public class SVGBuilder {

	/**
	 * @param chart JFreeChart object
	 * @param name name of the svg file
	 */
	public static void saveSVG(JFreeChart chart, String name) {
		File f = new File("plots" + File.separator + "svg");
		f.mkdirs();

		SVGGraphics2D svgGraphics2D = new SVGGraphics2D(1920, 1080);
		chart.draw(svgGraphics2D, new Rectangle(1920, 1080));

		String svgElement = svgGraphics2D.getSVGElement();
		try {
			SVGUtils.writeToSVG(new File("plots" + File.separator + "svg" + File.separator + name + ".svg"),
					svgElement);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param chart1 JFreeChart object
	 * @param chart2 JFreeChart object
	 * @param name name of the svg file
	 */
	public static void saveSVG(JFreeChart chart1, JFreeChart chart2, String name) {
		File f = new File("plots" + File.separator + "svg");
		f.mkdirs();

		SVGGraphics2D svgGraphics2D = new SVGGraphics2D(1920, 1080);
		chart1.draw(svgGraphics2D, new Rectangle(0, 0, 1920, 1080 / 2), null, null);
		chart2.draw(svgGraphics2D, new Rectangle(0, 1080 / 2, 1920, 1080 / 2), null, null);

		String svgElement = svgGraphics2D.getSVGElement();
		try {
			SVGUtils.writeToSVG(new File("plots" + File.separator + "svg" + File.separator + name + ".svg"),
					svgElement);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param chart1 JFreeChart object
	 * @param chart2 JFreeChart object
	 * @param chart3 JFreeChart object
	 * @param chart4 JFreeChart object
	 * @param name name of the svg file
	 */
	public static void saveSVG(JFreeChart chart1, JFreeChart chart2, JFreeChart chart3, JFreeChart chart4,
			String name) {
		File f = new File("plots" + File.separator + "svg");
		f.mkdirs();

		SVGGraphics2D svgGraphics2D = new SVGGraphics2D(2 * 1920, 2 * 1080);
		chart1.draw(svgGraphics2D, new Rectangle(0, 0, 1920, 1080), null, null);
		chart2.draw(svgGraphics2D, new Rectangle(1920, 0, 1920, 1080), null, null);
		chart3.draw(svgGraphics2D, new Rectangle(0, 1080, 1920, 1080), null, null);
		chart4.draw(svgGraphics2D, new Rectangle(1920, 1080, 1920, 1080), null, null);

		String svgElement = svgGraphics2D.getSVGElement();
		try {
			SVGUtils.writeToSVG(new File("plots" + File.separator + "svg" + File.separator + name + ".svg"),
					svgElement);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param chart1 JFreeChart object
	 * @param chart2 JFreeChart object
	 * @param chart3 JFreeChart object
	 * @param chart4 JFreeChart object
	 * @param chart5 JFreeChart object
	 * @param chart6 JFreeChart object
	 * @param chart7 JFreeChart object
	 * @param chart8 JFreeChart object
	 * @param name name of the svg file
	 */
	public static void saveSVG(JFreeChart chart1, JFreeChart chart2, JFreeChart chart3, JFreeChart chart4,
			JFreeChart chart5, JFreeChart chart6, JFreeChart chart7, JFreeChart chart8, String name) {
		File f = new File("plots" + File.separator + "svg");
		f.mkdirs();

		SVGGraphics2D svgGraphics2D = new SVGGraphics2D(2 * 1920, 4 * 1080);
		chart1.draw(svgGraphics2D, new Rectangle(0, 0, 1920, 1080), null, null);
		chart2.draw(svgGraphics2D, new Rectangle(1920, 0, 1920, 1080), null, null);
		chart3.draw(svgGraphics2D, new Rectangle(0, 1080, 1920, 1080), null, null);
		chart4.draw(svgGraphics2D, new Rectangle(1920, 1080, 1920, 1080), null, null);
		chart5.draw(svgGraphics2D, new Rectangle(0, 2 * 1080, 1920, 1080), null, null);
		chart6.draw(svgGraphics2D, new Rectangle(1920, 2* 1080, 1920, 1080), null, null);
		chart7.draw(svgGraphics2D, new Rectangle(0, 3 * 1080, 1920, 1080), null, null);
		chart8.draw(svgGraphics2D, new Rectangle(1920, 3* 1080, 1920, 1080), null, null);
		
		String svgElement = svgGraphics2D.getSVGElement();
		try {
			SVGUtils.writeToSVG(new File("plots" + File.separator + "svg" + File.separator + name + ".svg"),
					svgElement);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
