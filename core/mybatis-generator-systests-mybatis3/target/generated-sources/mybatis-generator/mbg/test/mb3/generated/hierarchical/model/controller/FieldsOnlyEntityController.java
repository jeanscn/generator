/*
 * This class was generated by MyBatis Generator Vgosoft Edition.
 * 生成时间: 2021-05-08 18:00
 */
package mbg.test.mb3.generated.hierarchical.model.controller;

import com.vgosoft.web.controller.abs.AbsBaseController;
import com.vgosoft.web.respone.ResponseList;
import com.vgosoft.web.respone.ResponseSimple;
import com.vgosoft.web.respone.ResponseSimpleImpl;
import com.vgosoft.web.respone.ResponseSimpleList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.lang.reflect.Field;
import java.util.List;
import mbg.test.mb3.generated.hierarchical.model.example.subpackage.FieldsOnlyEntityExample;
import mbg.test.mb3.generated.hierarchical.model.service.IFieldsOnlyEntity;
import mbg.test.mb3.generated.hierarchical.model.subpackage.FieldsOnlyEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;



@RestController
@Api(value = "/fieldsonlyentity", tags = "null")
@RequestMapping(value = "/hierarchical")
class FieldsOnlyEntityController extends AbsBaseController {
    private IFieldsOnlyEntity fieldsOnlyEntityImpl;

    @Autowired
    public void setFieldsOnlyEntityImpl(IFieldsOnlyEntity fieldsOnlyEntityImpl) {
        this.fieldsOnlyEntityImpl = fieldsOnlyEntityImpl;
    }

    /**
    * 根据主键获取单个业务实例
    * @param id 可选参数，存在时查询数据；否则直接返回视图，用于打开表单。
    */
    @ApiOperation(value = "获得数据并返回页面视图（可用于普通业务在列表中新建接口）",notes = "根据给定id获取单个实体，id为可选参数，当id存在时查询数据，否则直接返回视图")
    @GetMapping(value = "fieldsOnlyEntityImpl/view")
    public ModelAndView viewFieldsOnlyEntity(@RequestParam(required = false) String id) {
        ModelAndView mv = new ModelAndView("hierarchical");
        try {
            if (id != null) {
                FieldsOnlyEntity fieldsonlyentity = fieldsOnlyEntityImpl.selectByPrimaryKey(id);
                mv.addObject("entity",fieldsonlyentity);
            }
        } catch (Exception e) {
            mv.setViewName("page/500");
            mv.addObject("error", e.getMessage());
        }
        return mv;
    }

    /** 根据主键获取单个实体 */
    @ApiOperation(value = "获得单条列表",notes = "根据给定id获取单个实体")
    @GetMapping(value = "fieldsOnlyEntityImpl/{id}")
    public ResponseSimple getFieldsOnlyEntity(@PathVariable String id) {
        ResponseSimple responseSimple = new ResponseSimpleImpl();
        try {
            FieldsOnlyEntity fieldsOnlyEntity = fieldsOnlyEntityImpl.selectByPrimaryKey(id);
            responseSimple.addAttribute("entity", fieldsOnlyEntity);
        }catch(Exception e){
            setExceptionResponse(responseSimple,e);
        }
        return responseSimple;
    }

    /**
    * 获取条件实体对象列表
    * @param fieldsOnlyEntity 用于接收属性同名参数
    */
    @ApiOperation(value = "获得数据列表",notes = "根据给定条件获取多条或所有数据列表，可以根据需要传入属性同名参数")
    @GetMapping(value = "fieldsOnlyEntityImpl")
    public ResponseSimple listFieldsOnlyEntity(FieldsOnlyEntity fieldsOnlyEntity) {
        ResponseList responseSimple = new ResponseSimpleList();
        FieldsOnlyEntityExample example = new FieldsOnlyEntityExample();
        try {
            if (fieldsOnlyEntity != null) {
                List<Field> list = getFieldsIsNotEmpty(fieldsOnlyEntity);
                if (list.size() > 0) {
                    
                }
            }
            List<FieldsOnlyEntity> fieldsOnlyEntitys = fieldsOnlyEntityImpl.selectByExample(example);
            responseSimple.setList(fieldsOnlyEntitys);
        }catch(Exception e){
            setExceptionResponse(responseSimple,e);
        }
        return responseSimple;
    }

    /** 新增一条记录 */
    @ApiOperation(value = "新增一条记录",notes = "新增一条记录,返回json")
    @PostMapping(value = "fieldsOnlyEntityImpl")
    public ResponseSimple createFieldsOnlyEntity(@RequestBody FieldsOnlyEntity fieldsOnlyEntity) {
        ResponseSimple responseSimple = new ResponseSimpleImpl();
        try {
            int rows =  fieldsOnlyEntityImpl.insert(fieldsOnlyEntity);
            if (rows<1) {
                responseSimple.setMessage("添加记录失败！");
                 }else{
                    responseSimple.addAttribute("rows",String.valueOf(rows));
                    responseSimple.setMessage("添加成功！");
                }
            }catch(Exception e){
                setExceptionResponse(responseSimple,e);
            }
            return responseSimple;
    }

    /** 根据主键更新实体对象 */
    @ApiOperation(value = "更新一条记录",notes = "根据主键更新实体对象")
    @PutMapping(value = "fieldsOnlyEntityImpl")
    public ResponseSimple updateFieldsOnlyEntity(@RequestBody FieldsOnlyEntity fieldsOnlyEntity) {
        ResponseSimple responseSimple = new ResponseSimpleImpl();
        try {
            int rows =  fieldsOnlyEntityImpl.updateByPrimaryKey(fieldsOnlyEntity);
            responseSimple.addAttribute("rows",String.valueOf(rows));
        }catch(Exception e){
            setExceptionResponse(responseSimple,e);
        }
        return responseSimple;
    }

    /** 删除一条记录 */
    @ApiOperation(value = "单条记录删除",notes = "根据给定的id删除一条记录")
    @DeleteMapping(value = "fieldsOnlyEntityImpl/{id}")
    public ResponseSimple deleteFieldsOnlyEntity(@PathVariable String id) {
        ResponseSimple responseSimple = new ResponseSimpleImpl();
        try {
            int rows =  fieldsOnlyEntityImpl.deleteByPrimaryKey(id);
            responseSimple.addAttribute("rows",String.valueOf(rows));
        }catch(Exception e){
            setExceptionResponse(responseSimple,e);
        }
        return responseSimple;
    }
}