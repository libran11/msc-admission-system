package com.msc.admission.enums;
//申请当前处于什么状态
// 草稿，已提交，审核中，需要补材料，已录取，已拒绝
public enum ApplicationStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    NEED_MORE_DOCUMENTS,
    ACCEPTED,
    REJECTED
}
