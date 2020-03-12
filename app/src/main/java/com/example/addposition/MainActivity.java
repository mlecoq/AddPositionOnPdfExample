package com.example.addposition;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.View;
import android.graphics.Color;
import android.graphics.Point;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;



import android.graphics.pdf.PdfRenderer;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static class MyDragShadowBuilder extends View.DragShadowBuilder {

        // The drag shadow image, defined as a drawable thing
        private static Drawable shadow;

        // Defines the constructor for myDragShadowBuilder
        public MyDragShadowBuilder(View v) {

            // Stores the View parameter passed to myDragShadowBuilder.
            super(v);

            // Creates a draggable image that will fill the Canvas provided by the system.
            shadow =v.getResources().getDrawable( R.drawable.target );
        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.

        public void onProvideShadowMetrics (Point size, Point touch) {
            // Defines local variables
            int width, height;

            // Sets the width of the shadow to half the width of the original View
            width = 80;

            // Sets the height of the shadow to half the height of the original View
            height = 80;

            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
            // Canvas that the system will provide. As a result, the drag shadow will fill the
            // Canvas.
            shadow.setBounds(0, 0, width, height);


            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);


            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(width / 2, height / 2);
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {

            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas);
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LinearLayout layout = findViewById(R.id.layout);


        try {

            final File file = new File(getApplication().getCacheDir(), "document.pdf");
            if (!file.exists()) {
                // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
                // the cache directory.
                final InputStream asset = getApplication().getAssets().open("document.pdf");
                final FileOutputStream output = new FileOutputStream(file);
                final byte[] buffer = new byte[1024];
                int size;
                while ((size = asset.read(buffer)) != -1) {
                    output.write(buffer, 0, size);
                }
                asset.close();
                output.close();
            }


            // create a new renderer
            PdfRenderer renderer = new PdfRenderer( ParcelFileDescriptor.open( file, ParcelFileDescriptor.MODE_READ_ONLY ));

            // let us just render all pages
            final int pageCount = renderer.getPageCount();
            System.out.println("page count : "+pageCount);
            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);

                int screenWidth = getResources().getSystem().getDisplayMetrics().widthPixels;

                System.out.println("Layout : "+screenWidth);
                System.out.println("Page : "+page.getWidth());
                System.out.println("ratio : "+(float)page.getWidth() /screenWidth);

                System.out.println("Height : "+Math.round((float)page.getWidth() /screenWidth) * page.getHeight());

                Bitmap bitmap = Bitmap.createBitmap(screenWidth, Math.round(((float) screenWidth /page.getWidth()) * page.getHeight()), Bitmap.Config.ARGB_4444);


                // say we render for showing on the screen
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

               final RelativeLayout relativeLayout = new RelativeLayout(this.getBaseContext());
               relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
               relativeLayout.setHorizontalGravity(1);


                relativeLayout.setOnDragListener(new View.OnDragListener() {
                    @Override
                    public boolean onDrag(View v, DragEvent event) {
                        final int action = event.getAction();
                        System.out.println(action);

                        // Handles each of the expected events
                        switch(action) {

                            case DragEvent.ACTION_DROP:

                                View view = new View(v.getContext());
                                view.setAlpha(0.5f);
                                view.setBackgroundColor(Color.BLUE);
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);

                                view.setLayoutParams(params);

                                view.setX(event.getX());
                                view.setY(event.getY());

                                relativeLayout.addView(view);

                                // Invalidates the view to force a redraw
                                v.invalidate();

                                // Returns true. DragEvent.getResult() will return true.
                                return true;


                            default:
                                return true;
                        }


                    }
                });

                // do stuff with the bitmap
                ImageView imageView = new ImageView(this.getBaseContext());

                imageView.setImageBitmap(bitmap);
                imageView.invalidate();

                relativeLayout.addView(imageView);

                layout.addView(relativeLayout);

                // close the page
                page.close();
            }

            // close the renderer
            renderer.close();
        }catch(Exception err){
            System.out.println("-------");
            System.out.println(err);
        }



        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {  // Instantiates the drag shadow builder.
                View.DragShadowBuilder myShadow = new MyDragShadowBuilder(v);

                // Starts the drag
                v.startDrag(null,  // the data to be dragged
                        myShadow,  // the drag shadow builder
                        null,      // no need to use local data
                        0          // flags (not currently used, set to 0)
                );

                return true;
            }


        });


    }

}
