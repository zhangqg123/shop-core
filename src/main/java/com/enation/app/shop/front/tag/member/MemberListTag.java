package com.enation.app.shop.front.tag.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.model.MemberLv;
import com.enation.app.base.core.service.IMemberManager;
import com.enation.app.shop.core.member.service.IMemberLvManager;
import com.enation.app.shop.core.order.model.support.CartItem;
import com.enation.app.shop.core.order.model.support.OrderPrice;
import com.enation.app.shop.core.order.plugin.cart.CartPluginBundle;
import com.enation.app.shop.core.order.service.ICartManager;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.eop.sdk.context.UserConext;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;

@Component
public class MemberListTag extends BaseFreeMarkerTag {

	@Autowired
	private IMemberManager memberManager;
	@Autowired
	private IMemberLvManager memberLvManager;
	@Autowired
	private ICartManager cartManager;
	@Autowired
	private CartPluginBundle cartPluginBundle; 

	@Override
	protected Object exec(Map params) throws TemplateModelException {
		List<MemberLv> lvlist = this.memberLvManager.list();
		Integer lvId = 5 ;
//		for(int i = 0;i < lvlist.size(); i ++){
//			if(lvlist.get(i).getName().equals(EopSetting.TABLE)){
//				lvId=lvlist.get(i).getLv_id();
//			}
//		}
		HashMap memberMap = new HashMap();
		memberMap.put("lvId", lvId);
		List<Member> list = this.memberManager.search(memberMap);
		for(int i = 0;i < list.size(); i ++){
			Member member = list.get(i);
			String memberid = member.getMember_id().toString();
			ThreadContextHolder.getSession().setAttribute(UserConext.CURRENT_TABLE_KEY,memberid);
			List<CartItem> itemList  = this.cartManager.selectListGoods(memberid);
			if(itemList==null||itemList.size()==0){
				member.setGoods_price(0.0);
			}else{
				OrderPrice orderPrice =   this.cartManager.countPrice(itemList, null, null);
				
				//激发价格计算事件
				orderPrice  = this.cartPluginBundle.coutPrice(orderPrice);
				member.setGoods_price(orderPrice.getGoodsPrice());
			}
		}
		return list;
	}

}
