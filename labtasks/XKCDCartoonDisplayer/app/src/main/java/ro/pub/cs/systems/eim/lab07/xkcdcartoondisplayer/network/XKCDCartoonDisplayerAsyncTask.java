package ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.entities.XKCDCartoonInformation;
import ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.general.Constants;

public class XKCDCartoonDisplayerAsyncTask extends AsyncTask<String, Void, XKCDCartoonInformation> {

    private TextView xkcdCartoonTitleTextView;
    private ImageView xkcdCartoonImageView;
    private TextView xkcdCartoonUrlTextView;
    private Button previousButton, nextButton;

    private class XKCDCartoonButtonClickListener implements Button.OnClickListener {

        private String xkcdComicUrl;

        public XKCDCartoonButtonClickListener(String xkcdComicUrl) {
            this.xkcdComicUrl = xkcdComicUrl;
        }

        @Override
        public void onClick(View view) {
            new XKCDCartoonDisplayerAsyncTask(xkcdCartoonTitleTextView, xkcdCartoonImageView, xkcdCartoonUrlTextView, previousButton, nextButton).execute(xkcdComicUrl);
        }

    }

    public XKCDCartoonDisplayerAsyncTask(TextView xkcdCartoonTitleTextView, ImageView xkcdCartoonImageView, TextView xkcdCartoonUrlTextView, Button previousButton, Button nextButton) {
        this.xkcdCartoonTitleTextView = xkcdCartoonTitleTextView;
        this.xkcdCartoonImageView = xkcdCartoonImageView;
        this.xkcdCartoonUrlTextView = xkcdCartoonUrlTextView;
        this.previousButton = previousButton;
        this.nextButton = nextButton;
    }

    @Override
    public XKCDCartoonInformation doInBackground(String... urls) {
        XKCDCartoonInformation xkcdCartoonInformation = new XKCDCartoonInformation();
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGetXKCD = new HttpGet(urls[0]);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String pageSourceCode = null;

        try {
            pageSourceCode = httpClient.execute(httpGetXKCD, responseHandler);
        } catch (IOException ioException) {
            Log.e(Constants.TAG, ioException.getMessage());
        }

        if (pageSourceCode != null) {
            Document document = Jsoup.parse(pageSourceCode);
            Element htmlTag = document.child(0);

            Element title = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.CTITLE_VALUE).first();
            xkcdCartoonInformation.setCartoonTitle(title.ownText());

            Element comicId = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.COMIC_VALUE).first();
            String uri = comicId.getElementsByTag(Constants.IMG_TAG).attr(Constants.SRC_ATTRIBUTE);
            String url = Constants.HTTP_PROTOCOL + uri;
            xkcdCartoonInformation.setCartoonUrl(url);

            try {
                HttpGet cartoon = new HttpGet(url);
                HttpResponse httpResponse = httpClient.execute(cartoon);
                HttpEntity httpEntity = httpResponse.getEntity();
                if (httpEntity != null) {
                    xkcdCartoonInformation.setCartoonBitmap(BitmapFactory.decodeStream(httpEntity.getContent()));
                }
            } catch (IOException ioException) {
                Log.e(Constants.TAG, ioException.getMessage());
            }

            Element prev = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.PREVIOUS_VALUE).first();
            String prevUri = Constants.XKCD_INTERNET_ADDRESS + prev.attr(Constants.HREF_ATTRIBUTE);
            xkcdCartoonInformation.setPreviousCartoonUrl(prevUri);

            Element next = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.NEXT_VALUE).first();
            String nextUri = Constants.XKCD_INTERNET_ADDRESS + next.attr(Constants.HREF_ATTRIBUTE);
            xkcdCartoonInformation.setNextCartoonUrl(nextUri);
        }

        return  xkcdCartoonInformation;
    }

    @Override
    protected void onPostExecute(final XKCDCartoonInformation xkcdCartoonInformation) {

        // TODO exercise 5b)
        // map each member of xkcdCartoonInformation object to the corresponding widget
        // cartoonTitle -> xkcdCartoonTitleTextView
        // cartoonBitmap -> xkcdCartoonImageView (only if using Apache HTTP Components)
        // cartoonUrl -> xkcdCartoonUrlTextView
        // based on cartoonUrl fetch the bitmap
        // and put it into xkcdCartoonImageView
        // previousCartoonUrl, nextCartoonUrl -> set the XKCDCartoonUrlButtonClickListener for previousButton, nextButton

        if (xkcdCartoonInformation != null) {
            String cartoonTitle = xkcdCartoonInformation.getCartoonTitle();
            if (cartoonTitle != null) {
                xkcdCartoonTitleTextView.setText(cartoonTitle);
            }
            Bitmap cartoonBitmap = xkcdCartoonInformation.getCartoonBitmap();
            if (cartoonBitmap != null) {
                xkcdCartoonImageView.setImageBitmap(cartoonBitmap);
            }
            String cartoonUrl = xkcdCartoonInformation.getCartoonUrl();
            if (cartoonUrl != null) {
                xkcdCartoonUrlTextView.setText(cartoonUrl);

            }
            String previousCartoonUrl = xkcdCartoonInformation.getPreviousCartoonUrl();
            if (previousCartoonUrl != null) {
                previousButton.setOnClickListener(new XKCDCartoonButtonClickListener(previousCartoonUrl));
            }
            String nextCartoonUrl = xkcdCartoonInformation.getNextCartoonUrl();
            if (nextCartoonUrl != null) {
                nextButton.setOnClickListener(new XKCDCartoonButtonClickListener(nextCartoonUrl));
            }
        }
    }
}
