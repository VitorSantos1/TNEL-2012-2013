package ei.service.negotiation.qfnegotiation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;


/**
 * Shows information over a need negotiation in chart. 
 * @author pbn
 */
public class NegotiationsChart extends JFrame{
	private static final long serialVersionUID = 1L;
	
	private XYSeriesCollection dataset;
	private String needClassType;
	private static int numberOfRounds;
	
	public NegotiationsChart(QFNegotiationMediatorGUI parent, String negotiationID, String needClassType, String txt) {
		super("Negotiation on "+ needClassType +": "+negotiationID+ " "+txt);
		
		this.needClassType = needClassType;
		
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		
		dataset = new XYSeriesCollection();
	}
	public void populateAgent(List<String> ea){
		if (!(dataset.getSeriesCount() > 0)) {
			XYSeries[] series = new XYSeries[ea.size()];
			for(int j=0;j<ea.size();j++){
				series[j] = new XYSeries(ea.get(j));
				series[j].setDescription(ea.get(j));
				dataset.addSeries(series[j]);
			}
		}
	}
	
	/**
	 * Adds a new value to the chart
	 */
	public void addValue(String agent, int x, float y){
		for(int i=0;i<dataset.getSeriesCount();i++){
			if(dataset.getSeries(i).getDescription().equals(agent)){
				dataset.getSeries(i).add(x,y);
			}
		}
	}	

	private static JFreeChart createChart(XYSeriesCollection dataset) {
		// create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
				"Utility",// chart title
				"round", // Y axis label
				"value", // X axis label
				dataset,// data
				PlotOrientation.VERTICAL,
				true,// include legend
				true,// tooltips
				false// urls
		);
		//CUSTOMISATION OF THE CHART...
		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customisation...
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setAxisOffset(new RectangleInsets(10.0, 10.0, 10.0, 10.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		
		if (numberOfRounds <= 10) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();  
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			
			org.jfree.chart.renderer.xy.XYItemRenderer xyr = chart.getXYPlot().getRenderer();  
			renderer.setSeriesStroke(0, new BasicStroke(1.3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND,1.0f, new float[] { 1.0f, 2.0f}, 0.0f));
			renderer.setSeriesStroke(1, new BasicStroke(1.3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND,1.0f, new float[] { 4.0f, 2.0f}, 0.0f));				
			renderer.setSeriesStroke(2, new BasicStroke(1.3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND,1.0f, new float[] { 7.0f, 2.0f}, 0.0f));				
			renderer.setSeriesStroke(3, new BasicStroke(1.3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND,1.0f, new float[] { 8.0f, 4.0f}, 0.0f));				
			renderer.setSeriesStroke(4, new BasicStroke(1.3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND,1.0f, new float[] { 8.0f, 6.0f}, 0.0f));				
		}
		
		NumberAxis rangeAxisEvaluation = (NumberAxis) plot.getRangeAxis();  
		//rangeAxisEvaluation.setAutoRange(true);  
		rangeAxisEvaluation.setRange(0.0, 1.0);
		rangeAxisEvaluation.setTickUnit( new NumberTickUnit(0.1) );  	
		
		NumberAxis rangeAxisRound = (NumberAxis) plot.getDomainAxis();
		//rangeAxisRound.setAutoRange(true);
		//rangeAxisRound.setRange(1, numberOfRounds);
		rangeAxisRound.setTickUnit(pickNumberTickUnit(numberOfRounds));

		return chart;
	}
	
	/**
	 * Shows this window
	 */
	public void showGui(){
		JFreeChart chart = createChart(dataset);

		ChartPanel chartPanel = new ChartPanel(chart, 620, 350, 300, 300, 620, 350, false, false, false, false, false, false);
		chartPanel.setRangeZoomable(false);

		setContentPane(chartPanel);
		pack();
		setVisible(true);
	}	
	
	private static NumberTickUnit pickNumberTickUnit (int numberOfRounds) {
		float tick;
		if (numberOfRounds < 30) {
			tick = 1.0f;
		} else if (numberOfRounds >= 30 && numberOfRounds <= 50) {
			tick = 2.0f;
		} else if (numberOfRounds <= 100 ) {
			tick = 4.0f; 
		} else if (numberOfRounds <= 150) {
			tick = 6.0f;			
		} else if (numberOfRounds <= 200) {
			tick = 8.0f;			
		} else if (numberOfRounds <= 300) {
			tick = 10.0f;
		} else if (numberOfRounds <= 500) {
					tick = 25.0f;			
		} else {
			tick = 50.0f;
		}
		return new NumberTickUnit(tick);
	}
	
	/**
	 * Gets the needClassType for this negotiation info window
	 */
	public String getNeedClassType() {
		return needClassType;
	}

	public void setNumberOfRounds(int numberOfRounds) {
		NegotiationsChart.numberOfRounds = numberOfRounds;
	}
	
 } 
