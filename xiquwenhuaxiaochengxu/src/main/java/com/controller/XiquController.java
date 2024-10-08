
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
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
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 戏曲
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/xiqu")
public class XiquController {
    private static final Logger logger = LoggerFactory.getLogger(XiquController.class);

    private static final String TABLE_NAME = "xiqu";

    @Autowired
    private XiquService xiquService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private CaozuorizhiService caozuorizhiService;//操作日志
    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private GonggaoService gonggaoService;//公告
    @Autowired
    private XiquCollectionService xiquCollectionService;//戏曲收藏
    @Autowired
    private XiquLiuyanService xiquLiuyanService;//戏曲留言
    @Autowired
    private XiqukepuService xiqukepuService;//戏曲科普
    @Autowired
    private YonghuService yonghuService;//用户
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        params.put("xiquDeleteStart",1);params.put("xiquDeleteEnd",1);
        CommonUtil.checkMap(params);
        PageUtils page = xiquService.queryPage(params);

        //字典表数据转换
        List<XiquView> list =(List<XiquView>)page.getList();
        for(XiquView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"列表查询",list.toString());
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        XiquEntity xiqu = xiquService.selectById(id);
        if(xiqu !=null){
            //entity转view
            XiquView view = new XiquView();
            BeanUtils.copyProperties( xiqu , view );//把实体数据重构到view中
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
    caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"单条数据查看",view.toString());
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody XiquEntity xiqu, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,xiqu:{}",this.getClass().getName(),xiqu.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<XiquEntity> queryWrapper = new EntityWrapper<XiquEntity>()
            .eq("xiqu_name", xiqu.getXiquName())
            .eq("xiqu_address", xiqu.getXiquAddress())
            .eq("xiqu_music", xiqu.getXiquMusic())
            .eq("xiqu_video", xiqu.getXiquVideo())
            .eq("xiqu_types", xiqu.getXiquTypes())
            .eq("zan_number", xiqu.getZanNumber())
            .eq("cai_number", xiqu.getCaiNumber())
            .eq("shangxia_types", xiqu.getShangxiaTypes())
            .eq("xiqu_delete", xiqu.getXiquDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        XiquEntity xiquEntity = xiquService.selectOne(queryWrapper);
        if(xiquEntity==null){
            xiqu.setXiquClicknum(1);
            xiqu.setShangxiaTypes(1);
            xiqu.setXiquDelete(1);
            xiqu.setInsertTime(new Date());
            xiqu.setCreateTime(new Date());
            xiquService.insert(xiqu);
            caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"新增",xiqu.toString());
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody XiquEntity xiqu, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,xiqu:{}",this.getClass().getName(),xiqu.toString());
        XiquEntity oldXiquEntity = xiquService.selectById(xiqu.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
        if("".equals(xiqu.getXiquPhoto()) || "null".equals(xiqu.getXiquPhoto())){
                xiqu.setXiquPhoto(null);
        }
        if("".equals(xiqu.getXiquFile()) || "null".equals(xiqu.getXiquFile())){
                xiqu.setXiquFile(null);
        }
        if("".equals(xiqu.getXiquVideo()) || "null".equals(xiqu.getXiquVideo())){
                xiqu.setXiquVideo(null);
        }

            xiquService.updateById(xiqu);//根据id更新
            List<String> strings = caozuorizhiService.clazzDiff(xiqu, oldXiquEntity, request,new String[]{"updateTime"});
            caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"修改",strings.toString());
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<XiquEntity> oldXiquList =xiquService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        ArrayList<XiquEntity> list = new ArrayList<>();
        for(Integer id:ids){
            XiquEntity xiquEntity = new XiquEntity();
            xiquEntity.setId(id);
            xiquEntity.setXiquDelete(2);
            list.add(xiquEntity);
        }
        if(list != null && list.size() >0){
            xiquService.updateBatchById(list);
        }

        caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"删除",oldXiquList.toString());
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<XiquEntity> xiquList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            XiquEntity xiquEntity = new XiquEntity();
//                            xiquEntity.setXiquName(data.get(0));                    //戏曲名称 要改的
//                            xiquEntity.setXiquUuidNumber(data.get(0));                    //戏曲编号 要改的
//                            xiquEntity.setXiquPhoto("");//详情和图片
//                            xiquEntity.setXiquAddress(data.get(0));                    //发源地 要改的
//                            xiquEntity.setXiquFile(data.get(0));                    //戏曲文件 要改的
//                            xiquEntity.setXiquMusic(data.get(0));                    //戏曲音频 要改的
//                            xiquEntity.setXiquVideo(data.get(0));                    //戏曲视频 要改的
//                            xiquEntity.setXiquTypes(Integer.valueOf(data.get(0)));   //戏曲类型 要改的
//                            xiquEntity.setZanNumber(Integer.valueOf(data.get(0)));   //赞 要改的
//                            xiquEntity.setCaiNumber(Integer.valueOf(data.get(0)));   //踩 要改的
//                            xiquEntity.setXiquClicknum(Integer.valueOf(data.get(0)));   //戏曲热度 要改的
//                            xiquEntity.setXiquContent("");//详情和图片
//                            xiquEntity.setShangxiaTypes(Integer.valueOf(data.get(0)));   //是否上架 要改的
//                            xiquEntity.setXiquDelete(1);//逻辑删除字段
//                            xiquEntity.setInsertTime(date);//时间
//                            xiquEntity.setCreateTime(date);//时间
                            xiquList.add(xiquEntity);


                            //把要查询是否重复的字段放入map中
                                //戏曲编号
                                if(seachFields.containsKey("xiquUuidNumber")){
                                    List<String> xiquUuidNumber = seachFields.get("xiquUuidNumber");
                                    xiquUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> xiquUuidNumber = new ArrayList<>();
                                    xiquUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("xiquUuidNumber",xiquUuidNumber);
                                }
                        }

                        //查询是否重复
                         //戏曲编号
                        List<XiquEntity> xiquEntities_xiquUuidNumber = xiquService.selectList(new EntityWrapper<XiquEntity>().in("xiqu_uuid_number", seachFields.get("xiquUuidNumber")).eq("xiqu_delete", 1));
                        if(xiquEntities_xiquUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(XiquEntity s:xiquEntities_xiquUuidNumber){
                                repeatFields.add(s.getXiquUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [戏曲编号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        xiquService.insertBatch(xiquList);
                        caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"批量新增",xiquList.toString());
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }



    /**
    * 个性推荐
    */
    @IgnoreAuth
    @RequestMapping("/gexingtuijian")
    public R gexingtuijian(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("gexingtuijian方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        CommonUtil.checkMap(params);
        List<XiquView> returnXiquViewList = new ArrayList<>();

        //查看收藏
        Map<String, Object> params1 = new HashMap<>(params);params1.put("sort","id");params1.put("yonghuId",request.getSession().getAttribute("userId"));
        PageUtils pageUtils = xiquCollectionService.queryPage(params1);
        List<XiquCollectionView> collectionViewsList =(List<XiquCollectionView>)pageUtils.getList();
        Map<Integer,Integer> typeMap=new HashMap<>();//购买的类型list
        for(XiquCollectionView collectionView:collectionViewsList){
            Integer xiquTypes = collectionView.getXiquTypes();
            if(typeMap.containsKey(xiquTypes)){
                typeMap.put(xiquTypes,typeMap.get(xiquTypes)+1);
            }else{
                typeMap.put(xiquTypes,1);
            }
        }
        List<Integer> typeList = new ArrayList<>();//排序后的有序的类型 按最多到最少
        typeMap.entrySet().stream().sorted((o1, o2) -> o2.getValue() - o1.getValue()).forEach(e -> typeList.add(e.getKey()));//排序
        Integer limit = Integer.valueOf(String.valueOf(params.get("limit")));
        for(Integer type:typeList){
            Map<String, Object> params2 = new HashMap<>(params);params2.put("xiquTypes",type);
            PageUtils pageUtils1 = xiquService.queryPage(params2);
            List<XiquView> xiquViewList =(List<XiquView>)pageUtils1.getList();
            returnXiquViewList.addAll(xiquViewList);
            if(returnXiquViewList.size()>= limit) break;//返回的推荐数量大于要的数量 跳出循环
        }
        //正常查询出来商品,用于补全推荐缺少的数据
        PageUtils page = xiquService.queryPage(params);
        if(returnXiquViewList.size()<limit){//返回数量还是小于要求数量
            int toAddNum = limit - returnXiquViewList.size();//要添加的数量
            List<XiquView> xiquViewList =(List<XiquView>)page.getList();
            for(XiquView xiquView:xiquViewList){
                Boolean addFlag = true;
                for(XiquView returnXiquView:returnXiquViewList){
                    if(returnXiquView.getId().intValue() ==xiquView.getId().intValue()) addFlag=false;//返回的数据中已存在此商品
                }
                if(addFlag){
                    toAddNum=toAddNum-1;
                    returnXiquViewList.add(xiquView);
                    if(toAddNum==0) break;//够数量了
                }
            }
        }else {
            returnXiquViewList = returnXiquViewList.subList(0, limit);
        }

        for(XiquView c:returnXiquViewList)
            dictionaryService.dictionaryConvert(c, request);
        page.setList(returnXiquViewList);
        return R.ok().put("data", page);
    }

    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = xiquService.queryPage(params);

        //字典表数据转换
        List<XiquView> list =(List<XiquView>)page.getList();
        for(XiquView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"列表查询",list.toString());
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        XiquEntity xiqu = xiquService.selectById(id);
            if(xiqu !=null){

                //点击数量加1
                xiqu.setXiquClicknum(xiqu.getXiquClicknum()+1);
                xiquService.updateById(xiqu);

                //entity转view
                XiquView view = new XiquView();
                BeanUtils.copyProperties( xiqu , view );//把实体数据重构到view中

                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                    caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"单条数据查看",view.toString());
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody XiquEntity xiqu, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,xiqu:{}",this.getClass().getName(),xiqu.toString());
        Wrapper<XiquEntity> queryWrapper = new EntityWrapper<XiquEntity>()
            .eq("xiqu_name", xiqu.getXiquName())
            .eq("xiqu_uuid_number", xiqu.getXiquUuidNumber())
            .eq("xiqu_address", xiqu.getXiquAddress())
            .eq("xiqu_music", xiqu.getXiquMusic())
            .eq("xiqu_video", xiqu.getXiquVideo())
            .eq("xiqu_types", xiqu.getXiquTypes())
            .eq("zan_number", xiqu.getZanNumber())
            .eq("cai_number", xiqu.getCaiNumber())
            .eq("xiqu_clicknum", xiqu.getXiquClicknum())
            .eq("shangxia_types", xiqu.getShangxiaTypes())
            .eq("xiqu_delete", xiqu.getXiquDelete())
//            .notIn("xiqu_types", new Integer[]{102})
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        XiquEntity xiquEntity = xiquService.selectOne(queryWrapper);
        if(xiquEntity==null){
                xiqu.setZanNumber(1);
                xiqu.setCaiNumber(1);
            xiqu.setXiquClicknum(1);
            xiqu.setXiquDelete(1);
            xiqu.setInsertTime(new Date());
            xiqu.setCreateTime(new Date());
        xiquService.insert(xiqu);

            caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"前台新增",xiqu.toString());
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

}

