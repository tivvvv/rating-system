package com.tiv.rating.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.entity.Comment;
import com.tiv.rating.system.service.CommentService;
import com.tiv.rating.system.mapper.CommentMapper;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService{

}




