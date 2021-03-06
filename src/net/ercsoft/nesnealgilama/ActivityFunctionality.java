package net.ercsoft.nesnealgilama;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import net.ercsoft.nesnealgilama.DetectionCore;

class ActivityFunctionality extends BaseClass {

	public static final int     VIEW_MODE_RGBA  = 0;
    public static final int     DETECT_BLUE_CIRCLE_TS = 6;
    
    
    private Mat mYuv;
    private Mat mRgba;
    private Mat mGraySubmat;
    private Mat mIntermediateMat;
    private Mat lines;
    List<MatOfPoint> contours;
	List<Rect> boxList;
	List<Mat>  signList;
	private Bitmap mBitmap;
	private int mViewMode;
	private int lineGap;
	private int minLineSize;
	private int threshold;
	private DetectionCore detect;


    public ActivityFunctionality(Context context) {
        super(context);
        mViewMode = VIEW_MODE_RGBA;
    }

	@Override
	protected void onPreviewStarted(int previewWidth, int previewHeight) {
	    synchronized (this) {
        	// initialize Mats before usage
        	mYuv = new Mat(getFrameHeight() + getFrameHeight() / 2, getFrameWidth(), CvType.CV_8UC1);
        	mGraySubmat = mYuv.submat(0, getFrameHeight(), 0, getFrameWidth());
        	
        	mRgba = new Mat();
        	mIntermediateMat = new Mat();
        	lines = new Mat();

        	mBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888); 
        	
        	contours = new ArrayList<MatOfPoint>();
        	boxList = new ArrayList<Rect>();
        	signList = new ArrayList<Mat>();
        	
        	detect = new DetectionCore();
        	
    	    }
	}

	@Override
	protected void onPreviewStopped() {
		if(mBitmap != null) {
			mBitmap.recycle();
		}

		synchronized (this) {
            // Explicitly deallocate Mats
            if (mYuv != null)
                mYuv.release();
            if (mRgba != null)
                mRgba.release();
            if (mGraySubmat != null)
                mGraySubmat.release();
            if (mIntermediateMat != null)
                mIntermediateMat.release();

            mYuv = null;
            mRgba = null;
            mGraySubmat = null;
            mIntermediateMat = null;
        }
    }

	
	/**
	 * major functionality
	 * processFrame method
	 * - it depends on selected value from menu
	 * - work intime, with camera view
	 * lines detect :
	 * http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#houghlinesp
	 */
	
    @Override
    protected Bitmap processFrame(byte[] data) {
        mYuv.put(0, 0, data);

        final int viewMode = mViewMode;

        switch (viewMode) {
        
        case VIEW_MODE_RGBA:
            Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);
            break;
   
        case DETECT_BLUE_CIRCLE_TS:
        	
        	
        	//Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);
            Imgproc.cvtColor(mGraySubmat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);

	        detect.setData(mRgba);
		    detect.detectBlueCircleSign();
		    boxList.clear();
	    	boxList = detect.getBoxList();
	    	signList.clear();
	    	signList = detect.getSignList();
		    //draw 
			for(int i = 0; i < boxList.size(); i++){
			  Rect r=boxList.get(i);
			  Core.rectangle(mRgba, r.tl(), r.br(), new Scalar(0, 255, 0, 255), 3);
			}
            break;

        	
        	///////
            /*
        	Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);
	        detect.setData(mRgba);
		    detect.detectAllSign();
		    boxList.clear();
	    	boxList = detect.getBoxList();
	    	signList.clear();
	    	signList = detect.getSignList();
		    //draw 
			for(int i = 0; i < boxList.size(); i++){
			  Rect r=boxList.get(i);
			  Core.rectangle(mRgba, r.tl(), r.br(), new Scalar(0, 255, 0, 255), 3);
			}
    		break;
        	
        	
        	*/
        	//////////
        }

        Bitmap bmp = mBitmap;
        
        try {
            Utils.matToBitmap(mRgba, bmp);
        } catch(Exception e) {
            Log.e("org.object.recognition", "Utils.matToBitmap() throws an exception: " + e.getMessage());
            bmp.recycle();
            bmp = null;
        }
        return bmp;
    }

    public void setViewMode(int viewMode) {
    	mViewMode = viewMode;
    }

}
