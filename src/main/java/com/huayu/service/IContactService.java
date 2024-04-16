package com.huayu.service;

import com.huayu.domain.Contact;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huayu.dto.Result;

/**
 * ContactService服务层
 */
public interface IContactService extends IService<Contact> {

    Result listContacts();

    Result removeContact(Integer contactId);

    Result blockContact(Integer contactId);

    Result nBlockContact(Integer contactId);
}
