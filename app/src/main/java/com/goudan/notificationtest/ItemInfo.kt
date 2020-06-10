package com.goudan.notificationtest

/**
 * 功能列表中每行item的信息
 */
class ItemInfo {
    // 是否为功能组标题
    var isSection : Boolean = false
    // 如果是功能组标题，则从本字段取值作为title显示
    var sectionStr : String = ""
    // 如果是普通item，则从本字段取值作为title显示
    var title : String = ""
}