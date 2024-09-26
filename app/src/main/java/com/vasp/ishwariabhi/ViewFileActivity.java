package com.vasp.ishwariabhi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;

import java.io.File;
import java.util.List;


public class ViewFileActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {

    private String _strFilePath, _strFileName;
    private TextView songName, songTime;
    private static int oTime = 0, sTime = 0, eTime = 0, fTime = 5000, bTime = 5000;
    private int playEnable = -1;
    private LinearLayout _llVideiViewlayer, _llPdfView;
    private boolean _isVideoPlaying;
    private boolean _isLandscapeMode;
    private ActionBar actionBar;
    private PDFView _pdfView;
    private Button _btnPrint,_btnShare;
    private PrintManager _printManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        try {
            actionBar = getSupportActionBar();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_view_file);
        try {
            _printManager = (PrintManager) getSystemService(PRINT_SERVICE);
            _llPdfView = findViewById(R.id.llpdf);
            _llPdfView.setVisibility(View.GONE);
            _pdfView = findViewById(R.id.pdfView);
            _strFilePath = getIntent().getStringExtra("FilePath");
            _strFileName = _strFilePath.substring(_strFilePath.lastIndexOf("/") + 1, _strFilePath.length());
            _btnPrint = findViewById(R.id.btnPrint);
            _btnShare = findViewById(R.id.btnShare);
            _btnPrint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    print(
                            new PdfDocumentAdapter(ViewFileActivity.this, _strFilePath),
                            new PrintAttributes.Builder().build());
//                    Uri uri = FileProvider.getUriForFile(ViewFileActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(_strFilePath));
//                    Intent share = new Intent();
//                    share.setAction(Intent.ACTION_SEND);
//                    share.setType("application/pdf");
//                    share.putExtra(Intent.EXTRA_STREAM, uri);
//                    startActivity(Intent.createChooser(share, "Share"));

                }
            });
            _btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    print(
//                            new PdfDocumentAdapter(ViewFileActivity.this, _strFilePath),
//                            new PrintAttributes.Builder().build());
                    Uri uri = FileProvider.getUriForFile(ViewFileActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(_strFilePath));
                    Intent share = new Intent();
                    share.setAction(Intent.ACTION_SEND);
                    share.setType("application/pdf");
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(share, "Share"));

                }
            });
            openAttachmentInApp(_strFilePath, _strFileName, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void print(PrintDocumentAdapter adapter,
                       PrintAttributes attrs) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        try {
            // PrintDocumentAdapter printAdapter = new PdfDocumentAdapter(this, _strFilePath );
            printManager.print("Document", adapter, new PrintAttributes.Builder().build());
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*startService(new Intent(this, PrintJobMonitorService.class));

        return(_printManager.print(_strFilePath, adapter, attrs));*/
    }

    private void openAttachmentInApp(String url1, String fileName, boolean isStreaming) {
        try {
            File url = new File(url1);
            //String fileUrl = IURL.BASE_URL_BROADCAST + fileName;
            // Log.d("LOG", "Rakshak openAttachmentInApp" + " url " + url.toString() + " weburl " + fileName + " file url " + fileUrl);
            if (url.toString().contains(".pdf")) {
                //pdfView.setVisibility(View.VISIBLE);
                _llPdfView.setVisibility(View.VISIBLE);
                displayFromUri();
                //TODO open pdf file
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    int pageNumber;

    private void displayFromUri() {
        try {
            Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", new File(_strFilePath));//getFileName(uri);

            _pdfView.fromUri(uri)
                    .defaultPage(pageNumber)
                    .onPageChange(this)
                    .enableAnnotationRendering(true)
                    .onLoad(this)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(10) // in dp
                    .onPageError(this)
                    .load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", _strFileName, page + 1, pageCount));
    }

    @Override
    public void loadComplete(int nbPages) {
        try {
            PdfDocument.Meta meta = _pdfView.getDocumentMeta();
            Log.e("Rakshak", "title = " + meta.getTitle());
            Log.e("Rakshak", "author = " + meta.getAuthor());
            Log.e("Rakshak", "subject = " + meta.getSubject());
            Log.e("Rakshak", "keywords = " + meta.getKeywords());
            Log.e("Rakshak", "creator = " + meta.getCreator());
            Log.e("Rakshak", "producer = " + meta.getProducer());
            Log.e("Rakshak", "creationDate = " + meta.getCreationDate());
            Log.e("Rakshak", "modDate = " + meta.getModDate());

            printBookmarksTree(_pdfView.getTableOfContents(), "-");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        try {
            for (PdfDocument.Bookmark b : tree) {

                Log.e("Rakshak", String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

                if (b.hasChildren()) {
                    printBookmarksTree(b.getChildren(), sep + "-");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e("Rakshak", "Cannot load page " + page);
    }
}