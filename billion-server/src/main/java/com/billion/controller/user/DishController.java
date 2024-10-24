package com.billion.controller.user;

import com.billion.constant.StatusConstant;
import com.billion.context.BaseContext;
import com.billion.entity.Dish;
import com.billion.result.Result;
import com.billion.service.DishService;
import com.billion.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {

        //构造redis中的key，规则：dish_分类id
        String key = "dish_" + categoryId;

        //查询redis中是否存在菜品数据
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if(list != null && !list.isEmpty()){
            //如果存在，直接返回，无须查询数据库
            return Result.success(list);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        //如果不存在，查询数据库，将查询到的数据放入redis中
        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key, list);

        return Result.success(list);
    }

    /**
     * 根据用户交互历史推荐菜品
     *
     * @return Result<List<DishVO>>
     */
    @GetMapping("/recommend")
    @ApiOperation("根据用户交互历史推荐菜品")
    public Result<List<DishVO>> recommend() {
        Long userId = BaseContext.getCurrentId();
        List<DishVO> list = dishService.listWithRecommendation(userId);
        return Result.success(list);
    }

    /**
     * 根据用户query搜索菜品
     *
     * @param query 搜索关键词
     * @return Result<List<DishVO>>
     */
    @GetMapping("/search")
    @ApiOperation("根据用户query搜索菜品")
    public Result<List<DishVO>> search(String query) {
        List<DishVO> list = dishService.listWithSearch(query);
        return Result.success(list);
    }

}
