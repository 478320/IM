package com.huayu.controller;


import com.huayu.dto.Result;
import com.huayu.service.IContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * contact表现层对象
 */
@RestController
@RequestMapping("/contact")
public class ContactController {

    @Autowired
    private IContactService contactService;

    /**
     * 获取我的所有联系人
     *
     * @return 所有联系人的Id和我是否屏蔽了他们的信息
     */
    @GetMapping("/get/me")
    public Result listContacts() {
        return contactService.listContacts();
    }

    /**
     * 删除联系人
     *
     * @param contactId 要删除的联系人的ID
     * @return 删除是否成功的结果
     */
    @DeleteMapping("/delete/{contactId}")
    public Result removeContact(@PathVariable("contactId") Integer contactId) {
        return contactService.removeContact(contactId);
    }

    /**
     * 屏蔽联系人
     *
     * @param contactId 要屏蔽的联系人的Id
     * @return 屏蔽是否成功的结果
     */
    @PutMapping("/block/{contactId}")
    public Result blockContact(@PathVariable("contactId") Integer contactId) {
        return contactService.blockContact(contactId);
    }

    /**
     * 取消屏蔽联系人
     *
     * @param contactId 要取消屏蔽的联系人的id
     * @return 取消屏蔽是否成功的结果
     */
    @PutMapping("/nBlock/{contactId}")
    public Result nBlockContact(@PathVariable("contactId") Integer contactId) {
        return contactService.nBlockContact(contactId);
    }
}
