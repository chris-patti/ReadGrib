/**
 * Simple program to read GRIB data using the UCAR NetCDF Java libraries
 *
 * @author Chris Patti
 * @version 1.0
 **/

package com.accuweather.grib;

import java.io.IOException;
import java.util.List;

import ucar.ma2.Array;
//import ucar.nc2.NetcdfFile;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.time.CalendarDateRange;

public class ReadGrib {

	public static void main(String[] args) {
		
		float lat=40.85f;
		float lon=-77.85f;
		String gribFile = "C:\\data\\ncep\\hrrr\\20200110\\hrrr.t00z.wrfsfcf00.grib2";
		
		// open GRIB file via GridDataset to use the geo-spatial tools to extract data at a specific lat/lon
		try {
			GridDataset gds = ucar.nc2.dt.grid.GridDataset.open(gribFile);
			
			CalendarDateRange dateRange = gds.getCalendarDateRange();
			
			System.out.println("Date Range " + dateRange.toString());
		
			List<VariableSimpleIF> vars = gds.getDataVariables();
			
			for (VariableSimpleIF var : vars) {
				System.out.println(var.getFullName() + " | " + var.getDescription() + " | " + var.getUnitsString());
			}
			
			double surfaceTemperature = getGridValue(lat, lon, "Temperature_height_above_ground", gds);
			double surfaceDewPoint = getGridValue(lat, lon, "Dewpoint_temperature_height_above_ground", gds);
			double surfaceVisiblity = getGridValue(lat, lon, "Visibility_surface", gds);
			double totalCloudCover = getGridValue(lat, lon, "Total_cloud_cover_entire_atmosphere", gds);
			double mslp = getGridValue(lat, lon, "MSLP_MAPS_System_Reduction_msl", gds);
			
			System.out.printf("Value at %f %f %s %5.2f %5.2f %4.1f %5.0f %5.0f%n", lat, lon, dateRange.getStart().toString(), 
					surfaceTemperature, surfaceDewPoint, totalCloudCover, surfaceVisiblity, mslp);
			
			gds.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		// traditional way of opening GRIB/NetCDF file
		 
		NetcdfFile ncfile = null;
		
		try {
			ncfile = NetcdfFile.open("C:\\data\\ncep\\gfs.t12z.pgrb2.0p25.f000");
			List<Variable> vars = ncfile.getVariables();
			//System.out.println(vars);
			
			Variable dataVar = ncfile.findVariable("Temperature_surface");
			//System.out.println(dataVar);
					    
			int[] shape = dataVar.getShape();
		    int[] origin = new int[shape.length];
		    
		    ArrayFloat.D3 values = (ArrayFloat.D3) dataVar.read(origin, shape);
		    
		    //System.out.println(values.get(0,200,700));
		    
		    dataVar = ncfile.findVariable("lat");
			//System.out.println(dataVar);
			
			shape = dataVar.getShape();
		    origin = new int[shape.length];
			
			ncfile.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}
	
	/**
	 * Method to return data value from a GRIB file given a latitude, longitude, data variable name, and opened GridDataset
	 * 
	 * @param latitude
	 * @param longitude
	 * @param dataVarName
	 * @param gds
	 * @return
	 */
	public static Double getGridValue(float latitude, float longitude, String dataVarName, GridDataset gds) {
		Double value = null;
		
		// get variable from GRIB file
		GridDatatype grid = gds.findGridDatatype(dataVarName);
		GridCoordSystem gcs = grid.getCoordinateSystem();
		
		// convert latitude/longitude to grid X/Y
		int [] coords = gcs.findXYindexFromLatLon(latitude, longitude, null);
		
		// read the data at that latitude, longitude and the first time and z level (if any) 
		try {
			Array data  = grid.readDataSlice(0, 0, coords[1], coords[0]); // note order is t, z, y, x
			value = data.getDouble(0); // we know its a scalar
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return value;
	}

}
