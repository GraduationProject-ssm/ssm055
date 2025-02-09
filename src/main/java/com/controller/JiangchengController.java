package com.controller;


import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.StringUtil;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;

import com.entity.JiangchengEntity;

import com.service.JiangchengService;
import com.entity.view.JiangchengView;
import com.service.YonghuService;
import com.entity.YonghuEntity;
import com.utils.PageUtils;
import com.utils.R;

/**
 * 奖惩
 * 后端接口
 * @author
 * @email
 * @date 2021-03-06
*/
@RestController
@Controller
@RequestMapping("/jiangcheng")
public class JiangchengController {
    private static final Logger logger = LoggerFactory.getLogger(JiangchengController.class);

    @Autowired
    private JiangchengService jiangchengService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;


    //级联表service
    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isNotEmpty(role) && "用户".equals(role)){
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        }
        PageUtils page = jiangchengService.queryPage(params);

        //字典表数据转换
        List<JiangchengView> list =(List<JiangchengView>)page.getList();
        for(JiangchengView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c);
        }
        return R.ok().put("data", page);
    }
    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        JiangchengEntity jiangcheng = jiangchengService.selectById(id);
        if(jiangcheng !=null){
            //entity转view
            JiangchengView view = new JiangchengView();
            BeanUtils.copyProperties( jiangcheng , view );//把实体数据重构到view中

            //级联表
            YonghuEntity yonghu = yonghuService.selectById(jiangcheng.getYonghuId());
            if(yonghu != null){
                BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody JiangchengEntity jiangcheng, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,jiangcheng:{}",this.getClass().getName(),jiangcheng.toString());
        Wrapper<JiangchengEntity> queryWrapper = new EntityWrapper<JiangchengEntity>()
            .eq("yonghu_id", jiangcheng.getYonghuId())
            .eq("jiangcheng_types", jiangcheng.getJiangchengTypes())
            .eq("jiangcheng_content", jiangcheng.getJiangchengContent())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        JiangchengEntity jiangchengEntity = jiangchengService.selectOne(queryWrapper);
        if(jiangchengEntity==null){
            jiangcheng.setCreateTime(new Date());
        //  String role = String.valueOf(request.getSession().getAttribute("role"));
        //  if("".equals(role)){
        //      jiangcheng.set
        //  }
            jiangchengService.insert(jiangcheng);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody JiangchengEntity jiangcheng, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,jiangcheng:{}",this.getClass().getName(),jiangcheng.toString());
        //根据字段查询是否有相同数据
        Wrapper<JiangchengEntity> queryWrapper = new EntityWrapper<JiangchengEntity>()
            .notIn("id",jiangcheng.getId())
            .eq("yonghu_id", jiangcheng.getYonghuId())
            .eq("jiangcheng_types", jiangcheng.getJiangchengTypes())
            .eq("jiangcheng_content", jiangcheng.getJiangchengContent())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        JiangchengEntity jiangchengEntity = jiangchengService.selectOne(queryWrapper);
        if(jiangchengEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      jiangcheng.set
            //  }
            jiangchengService.updateById(jiangcheng);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }


    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        jiangchengService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


}

