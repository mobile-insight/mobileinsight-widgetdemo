package net.mobileinsight.widgetdemo;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class UsageGuideFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_usage_guide, container, false);

        TextView tv_step1 = (TextView) rootView.findViewById(R.id.step1);
        TextView tv_step2 = (TextView) rootView.findViewById(R.id.step2);
        TextView tv_step3 = (TextView) rootView.findViewById(R.id.step3);
        TextView tv_step4 = (TextView) rootView.findViewById(R.id.step4);
        TextView tv_info = (TextView) rootView.findViewById(R.id.info);

//        String termsText = "By registering, you are agree to our";
//        String termsLink = " <a href=https://www.yourdomain.com/terms-conditions.html >Terms of Service</a>";
//        String privacyLink = " and our <a href=https://www.yourdomain.com/privacy-policy.html >Privacy Policy</a>";
//        String allText = termsText + termsLink + privacyLink;
//        tv_info.setMovementMethod(LinkMovementMethod.getInstance());

        if (Build.VERSION.SDK_INT >= 24)
        {
            tv_step1.setText(Html.fromHtml(getString(R.string.step1),Html.FROM_HTML_MODE_LEGACY));
            tv_step2.setText(Html.fromHtml(getString(R.string.step2),Html.FROM_HTML_MODE_LEGACY));
            tv_step3.setText(Html.fromHtml(getString(R.string.step3),Html.FROM_HTML_MODE_LEGACY));
            tv_step4.setText(Html.fromHtml(getString(R.string.step4),Html.FROM_HTML_MODE_LEGACY));
            tv_info.setText(Html.fromHtml(getString(R.string.info),Html.FROM_HTML_MODE_LEGACY));
        }
        else
        {
            tv_step1.setText(Html.fromHtml(getString(R.string.step1)));
            tv_step2.setText(Html.fromHtml(getString(R.string.step2)));
            tv_step3.setText(Html.fromHtml(getString(R.string.step3)));
            tv_step4.setText(Html.fromHtml(getString(R.string.step4)));
            tv_info.setText(Html.fromHtml(getString(R.string.info)));
        }

        return rootView;
    }
}
