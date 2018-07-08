package com.enation.app.shop.core.table.plugin;

import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.model.Table;

/**
 * 会员删除事件
 * @author zh
 * @version 1.0
 * @since v61
 * 2016年09月23号上午 9点10分
 */
public interface ITableDeleteEvent {
	/**
	 * 会员删除
	 * @param member 会员信息
	 */
	public void delteTable(Table table);


}
