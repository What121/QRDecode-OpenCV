package com.bestom.CameraQR.utils;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.opencv.core.CvType.CV_16S;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class OpenCVUtils {
    private int n=1;
    private Mat kernel = getStructuringElement(MORPH_RECT,new Size(2*n+1,2*n+1),new Point(n,n));
    private Mat element = getStructuringElement(0, new Size(10, 10));

    public OpenCVUtils() {
    }

    public Mat gray(Mat srcmat){
        //通道1 为灰度图
        Mat dstmat = new Mat();
        Log.e("dddd","通道"+ srcmat.channels());
        Imgproc.cvtColor(srcmat,dstmat,Imgproc.COLOR_BGR2GRAY);
        return dstmat;
    }

    public Mat gslb(Mat srcmat){
        Mat dstmat=new Mat();
        Imgproc.GaussianBlur(srcmat,dstmat,kernel.size(),0,0);
        return dstmat;
    }

    public Mat Sobel(Mat srcmat){
        Mat dstmat = new Mat();
        //求得水平和垂直方向灰度图像的梯度和,使用Sobel算子
        Mat imageX16S =new Mat();
        Mat imageSobelX =new Mat();
        Mat imageY16S =new Mat();
        Mat imageSobelY=new Mat();
        //计算水平方向灰度梯度绝对值
        Imgproc.Sobel(srcmat, imageX16S, CV_16S, 1, 0, 3, 1, 0, 4);
        Core.convertScaleAbs(imageX16S, imageSobelX,1,0);
        //计算垂直方向灰度梯度绝对值
        Imgproc.Sobel(srcmat, imageY16S, CV_16S, 0, 1, 3, 1, 0, 4);
        Core.convertScaleAbs(imageY16S, imageSobelY,1,0);
        //合并梯度
        Core.addWeighted(imageSobelX,0.5,imageSobelY,0.5,0,dstmat);

        return dstmat;
    }

    public Mat jzlb(Mat srcmat){
        Mat dstmat=new Mat();
        Imgproc.blur(srcmat,dstmat,kernel.size());
        return  dstmat;
    }

    public Mat threshold(Mat srcmat){
        Mat dstmat=new Mat();
        Imgproc.threshold(srcmat,dstmat,100,255,THRESH_BINARY);
        return dstmat;
    }

    public Mat bcl(Mat srcmat){
        Mat dstmat=new Mat();
        Imgproc.morphologyEx(srcmat,dstmat, MORPH_CLOSE, element);
        return dstmat;
    }

    public Mat fscl(Mat srcmat){
        Mat dstmat=new Mat();
        Imgproc.erode(srcmat,dstmat,element);
        return dstmat;
    }

    public Mat pzcl(Mat srcmat){
        Mat dstmat=new Mat();
        Imgproc.dilate(srcmat,dstmat,element);
        return dstmat;
    }

    public RotatedRect findContours(Mat srcmat){
        try {
            RotatedRect minRotatedRect = null;

            List<MatOfPoint> contours = new ArrayList<>();
            Mat hiera = new Mat();
            Imgproc.findContours(srcmat,contours,hiera,RETR_EXTERNAL,CHAIN_APPROX_SIMPLE);
            System.out.println("轮廓数量："+ contours.size());
            System.out.println("hierarchy类型："+ hiera);
            for (int i = 0; i<(contours.size()-1); i++)
            {
                //region 获取最大边缘边界
//            if (contourArea(contours.get(i))>contourArea(contours.get(i+1)))
//                rect = Imgproc.boundingRect(contours.get(i));
//            else
//                rect =  Imgproc.boundingRect(contours.get(i+1));
                //endregion

                //region 获取最小边缘界限
                if (contourArea(contours.get(i))>contourArea(contours.get(i+1)))
                    minRotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
                else
                    minRotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i+1).toArray()));
                //endregion

            }
            //endregion

            //region 角点定位处理
//        System.out.println("rect.tl"+ rect.tl().toString());
//        System.out.println("rect.br"+ rect.br().toString());
            System.out.println("findContours tl"+ minRotatedRect.boundingRect().tl().toString());
            System.out.println("findContours br"+ minRotatedRect.boundingRect().br().toString());
            System.out.println("findContours angle 角度"+ minRotatedRect.angle);

            return minRotatedRect;
        }catch (Exception e){
            return  null;
        }

    }

    public Mat jqcl(Mat srcmat, RotatedRect minRotatedRect){
        Point[] rectPoint = new Point[4];
        minRotatedRect.points(rectPoint);

        int startLeft = (int)Math.abs(rectPoint[0].x);
        int startUp = (int)Math.abs(rectPoint[0].y < rectPoint[1].y ? rectPoint[0].y : rectPoint[1].y);
        int width = (int)Math.abs(rectPoint[2].x - rectPoint[0].x);
        int height = (int)Math.abs(rectPoint[1].y - rectPoint[0].y);

        System.out.println("截取 startLeft = " + startLeft);
        System.out.println("截取 startUp = " + startUp);
        System.out.println("截取 width = " + width);
        System.out.println("截取 height = " + height);

        for(Point p : rectPoint) {
            if (p.x<0||p.y<0){
                return null;
            }
            System.out.println(p.x + " , " + p.y);
        }

//        Point point1=new Point(minRotatedRect.boundingRect().tl().x-200,minRotatedRect.boundingRect().tl().y+200);
//        Point point2=new Point(minRotatedRect.boundingRect().tl().x+200,minRotatedRect.boundingRect().tl().y-200);

        try {
            Mat dstmat=new Mat(srcmat , new Rect(minRotatedRect.boundingRect().tl() ,minRotatedRect.boundingRect().br() ));
            return dstmat;
        }catch (Exception e){
            return null;
        }
    }

    public Mat jzcl(Mat srcmat, RotatedRect minRotatedRect){
        Mat dstmat=new Mat();
        // 获取矩形的四个顶点
        Point[] minPoint = new Point[4];
        minRotatedRect.points(minPoint);
        double angle = minRotatedRect.angle + 90;
        Point center = minRotatedRect.center;

        // 得到旋转矩阵算子 第三个参数 是 缩放 因子 ，
        Mat matrix = Imgproc.getRotationMatrix2D(center, angle, 1.1);

        System.out.println("校正 angle = " + angle);
        System.out.println("校正 center = " + center.x +"*"+center.y);

        //仿射变换
        Imgproc.warpAffine(srcmat, dstmat, matrix, srcmat.size(), 1, 0, new Scalar(0, 0, 0));

        return dstmat;
    }









}
