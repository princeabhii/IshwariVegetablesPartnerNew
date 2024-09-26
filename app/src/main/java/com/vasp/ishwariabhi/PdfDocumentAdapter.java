package com.vasp.ishwariabhi;

import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * PdfDocumentAdapter
 * Created by Renuka Anil dated 8 December 2020
 */

public class PdfDocumentAdapter extends PrintDocumentAdapter {

  private Context context;
  private String pathName, fileName;

  public PdfDocumentAdapter(Context ctxt, String pathName) {
    context = ctxt;
    this.pathName = pathName;
  }
  @Override
  public void onLayout(PrintAttributes printAttributes, PrintAttributes printAttributes1, CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback, Bundle bundle) {
    if (cancellationSignal.isCanceled()) {
      layoutResultCallback.onLayoutCancelled();
    }
    else {
      fileName = pathName.substring(pathName.lastIndexOf("/")+1, pathName.length());
      PrintDocumentInfo.Builder builder=
              new PrintDocumentInfo.Builder(fileName);
      builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
              .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
              .build();
      layoutResultCallback.onLayoutFinished(builder.build(),
              !printAttributes1.equals(printAttributes));
    }
  }

  @Override
  public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor parcelFileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
    InputStream in=null;
    OutputStream out=null;
    try {
      File file = new File(pathName);
      in = new FileInputStream(file);
      out=new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

      byte[] buf=new byte[16384];
      int size;

      while ((size=in.read(buf)) >= 0
              && !cancellationSignal.isCanceled()) {
        out.write(buf, 0, size);
      }

      if (cancellationSignal.isCanceled()) {
        writeResultCallback.onWriteCancelled();
      }
      else {
        writeResultCallback.onWriteFinished(new PageRange[] { PageRange.ALL_PAGES });
      }
    }
    catch (Exception e) {
      writeResultCallback.onWriteFailed(e.getMessage());
      Log.d("RAKSHAK", " Exception while write "+e.toString());
    }
    finally {
      try {
        in.close();
        out.close();
      }
      catch (IOException e) {
        Log.d("RAKSHAK", " IOException in onWrite function  "+e.toString());
      }catch(Exception ex){
        Log.d("RAKSHAK", " Exception in onwrite function "+ex.toString());
      }
    }
  }
}
