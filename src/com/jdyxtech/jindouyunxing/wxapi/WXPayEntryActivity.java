package com.jdyxtech.jindouyunxing.wxapi;

import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.activity.Pay;
import com.jdyxtech.jindouyunxing.activity.PayFail;
import com.jdyxtech.jindouyunxing.activity.PaySuccess;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
/**
 * 微信支付结果展示界面（微信支付需要提供这么一个界面）
 * @author Tom
 *
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	
	private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pay_result);
        
    	api = WXAPIFactory.createWXAPI(this, "");
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@SuppressLint("NewApi") //builder.setOnDismissListener需要API >= 17,所以此处添加@SuppressLint("NewApi")
	@Override
	public void onResp(final BaseResp resp) {
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("支付结果");
			if (resp.errCode==0) {
				builder.setMessage("支付成功！");
			}else if (resp.errCode == -1) {
				builder.setMessage("支付失败！");
			}else if (resp.errCode == -2) {
				builder.setMessage("支付失败，您取消了支付！");
			}
			//AlertDialog的消失监听
			builder.setOnDismissListener(new OnDismissListener() { //API必须为 17 以上
				@Override
				public void onDismiss(DialogInterface dialog) {
					Intent intent = new Intent();
					if (resp.errCode==0) { 
						intent.setClass(WXPayEntryActivity.this, PaySuccess.class).putExtra("type", 1);//1 表示微信 支付方式,用于让下一个页面区分
					}else if (resp.errCode == -1) {
						intent.setClass(WXPayEntryActivity.this, PayFail.class).putExtra("type", 1);
					}else if (resp.errCode == -2) {
						intent.setClass(WXPayEntryActivity.this, PayFail.class).putExtra("type", 1);
					}
					startActivity(intent);
					Pay.pay.finish();//关闭支付界面
					finish(); 
				}
			});
			builder.show();
		}
	}
}