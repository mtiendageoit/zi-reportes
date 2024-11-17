package com.zonainmueble.reports.jasper;

import net.sf.jasperreports.charts.fill.JRFillPieDataset;
import net.sf.jasperreports.engine.JRAbstractChartCustomizer;
import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.fill.JRFillDataset;
import net.sf.jasperreports.engine.util.JRColorUtil;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;

import com.zonainmueble.reports.models.CategoryData;

public class PieChartCustomizer extends JRAbstractChartCustomizer {

  @Override
  public void customize(JFreeChart chart, JRChart jasperChart) {
    Plot plot = chart.getPlot();
    if (plot instanceof PiePlot) {
      PiePlot piePlot = (PiePlot) plot;
      JRFillPieDataset dataset = (JRFillPieDataset) jasperChart.getDataset();
      JRFillDataset fillDatase = dataset.getFillDataset();
      JRBeanCollectionDataSource source = (JRBeanCollectionDataSource) fillDatase
          .getParameterValue(JRParameter.REPORT_DATA_SOURCE);

      Collection<?> collection = source.getData();
      if (collection instanceof List<?>) {
        @SuppressWarnings("unchecked")
        List<CategoryData> data = (List<CategoryData>) collection;
        data.forEach(item -> piePlot.setSectionPaint(item.getCategory(), JRColorUtil.getColor(item.getColor(), Color.WHITE)));
      }
    }
  }
}
