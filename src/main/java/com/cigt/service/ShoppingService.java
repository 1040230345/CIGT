package com.cigt.service;

import com.cigt.base.R;
import com.cigt.dto.GoodsDto;
import com.cigt.dto.ShoppingDto;
import com.cigt.dto.UserDto;
import com.cigt.mapper.GoodsMapper;
import com.cigt.mapper.ShoppingMapper;
import com.cigt.my_util.GetTime_util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class ShoppingService {
    @Autowired
    private ShoppingMapper shoppingMapper;
    @Autowired
    private GetTime_util getTime_util;

    @Autowired
    private GoodsMapper goodsMapper;

    /**
     * 查询购物车信息
     * @param user_id
     * @return
     */
    public R allShoppingByUserId(int user_id){
        List<ShoppingDto> shoppingDtos = shoppingMapper.allShoppingByUserId(user_id);
        if(shoppingDtos != null){
            for(ShoppingDto shoppingDto:shoppingDtos) {
                GoodsDto goodsDtos = shoppingMapper.goodsById(shoppingDto.getGoods_id());
                shoppingDto.setGoodsDtos(goodsDtos);
            }
            return R.ok(shoppingDtos);
        }
           return R.error("购物车为空");
    }

    /**
     * 插入购物车事务
     */
    public R insertShopping(int goods_id,int number,String user_address,int userId){
        ShoppingDto shoppingDto = new ShoppingDto();
        shoppingDto.setCreated_at(getTime_util.GetNowTime_util());
        shoppingDto.setUpdated_at(getTime_util.GetNowTime_util());
        shoppingDto.setUser_id(userId);
        shoppingDto.setGoods_id(goods_id);
        shoppingDto.setNumber(number);
        shoppingDto.setUser_address(user_address);
        try {
            //先判断是否加入的数量大于存在的数量
            int goodsNum = goodsMapper.findGoodsNum(goods_id);
            if(number>goodsNum){
                return R.error("超过商品数量数量");
            }
            shoppingMapper.insertShopping(shoppingDto);
            return R.ok("加入购物车成功");
        }catch (Exception e){
            System.out.println(e);
            return R.error("加入购物车失败");
        }
    }

    /**
     * 删除购物车事务
     * @param id
     * @return
     */
    public R deleteShopping(int id){
        try {
        shoppingMapper.deleteShopping(id);
        return R.ok("移除购物车成功");
        }catch (Exception e){
        System.out.println(e);
        return R.error("移除购物车失败");
         }
    }

    /**
     * 修改购物车事务
     */
    public R updateShopping(int id,int number,String user_address){
      String update_time = getTime_util.GetNowTime_util();
        try {
            shoppingMapper.updateShopping(id,number,user_address,update_time);
            return R.ok("修改成功");
        }catch (Exception e){
            System.out.println(e);
            return R.error("修改失败");
        }
    }

    /**
     * 查看订单事务
     */
    public R allOrderByUserId(int user_id){
        List<ShoppingDto> shoppingDtos = shoppingMapper.allOrderByUserId(user_id);
        if(shoppingDtos != null){
            for(ShoppingDto shoppingDto:shoppingDtos) {
                GoodsDto goodsDtos = shoppingMapper.goodsById(shoppingDto.getGoods_id());
                shoppingDto.setGoodsDtos(goodsDtos);
            }
            return R.ok(shoppingDtos);
        }
        return R.error("订单为空");
    }
    /**
     * 立即购买事务
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public R successPayOne(int user_id,int goods_id,String user_address,int number){
        String created_at = getTime_util.GetNowTime_util();
        String updated_at = getTime_util.GetNowTime_util();
        int status = 1;
        try {
            //判断是否下单的数量大于（商品表锁行）
            int goodsNum = goodsMapper.findGoodsNum(goods_id);
            if(number>goodsNum){
                return R.error("购买的数量超过商品的数量");
            }
            //将商品减去相应的数量
            goodsMapper.updateGoodsNum(number,goods_id);
            if(number==goodsNum){
                //修改商品为下架
                goodsMapper.updateGoodsStatus(goods_id);
            }
            //加入订单表
            shoppingMapper.successPayOne(user_id,goods_id,number,user_address,status,created_at,updated_at);
            return R.ok("支付成功");
        }catch (Exception e){
            //主动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            //System.out.println(e);
            return R.error("支付失败");
        }
    }

    /**
     * 多个支付接口
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public R successPay(int[] id){
        String updated_at = getTime_util.GetNowTime_util();
        try {
            for( int i=0 ; i<id.length ; i++){
                //查询商品
                GoodsDto goodsDto = shoppingMapper.findGoodsByShoppingId(id[i]);
                //获取购物车信息
                ShoppingDto shoppingDto = shoppingMapper.findShoppingById(id[i]);
                //修改库存
                try {
                    if(goodsDto.getStatus()==1){
                        //主动回滚
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return R.error("操作了已经下架的商品");
                    }
                    int num = goodsMapper.updateGoodsNum(shoppingDto.getNumber(),goodsDto.getId());
                    if(num<1){
                        return R.error("操作了不存在的商品");
                    }
                }catch (Exception e){
                    //主动回滚
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return R.error("你购买的商品库存不足");
                }
                int num = shoppingMapper.successPay(id[i],updated_at);
                if(num==0){
                    //主动回滚
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return R.error("存在商品库存不足或已经失效");
                }

            }
            return R.ok("支付成功");
        }catch (Exception e){
            System.out.println(e);
            return R.error("支付失败");
        }
    }

}
