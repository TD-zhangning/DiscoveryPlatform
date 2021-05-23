package com.nepxion.discovery.platform.server.controller;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 *
 * @author Ning Zhang
 * @version 1.0
 */

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nepxion.discovery.common.entity.ResultEntity;
import com.nepxion.discovery.common.util.JsonUtil;
import com.nepxion.discovery.console.resource.RouteResource;
import com.nepxion.discovery.console.resource.ServiceResource;
import com.nepxion.discovery.platform.server.constant.PlatformConstant;
import com.nepxion.discovery.platform.server.entity.dto.RouteGatewayDto;
import com.nepxion.discovery.platform.server.entity.po.RouteGatewayPo;
import com.nepxion.discovery.platform.server.entity.response.Result;
import com.nepxion.discovery.platform.server.entity.vo.GatewayRouteVo;
import com.nepxion.discovery.platform.server.service.RouteGatewayService;
import com.nepxion.discovery.platform.server.tool.CommonTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping(RouteGatewayController.PREFIX)
public class RouteGatewayController {
    public static final String PREFIX = "routegateway";

    @Autowired
    private ServiceResource serviceResource;

    @Autowired
    private RouteResource routeResource;

    @Autowired
    private RouteGatewayService routeGatewayService;

    @GetMapping("list")
    public String list() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("working")
    public String working(final Model model) {
        model.addAttribute("gatewayNames", this.serviceResource.getGatewayList(RouteGatewayService.GATEWAY_TYPE));
        return String.format("%s/%s", PREFIX, "working");
    }

    @GetMapping("add")
    public String add(final Model model) {
        model.addAttribute("gatewayNames", this.serviceResource.getGatewayList(RouteGatewayService.GATEWAY_TYPE));
        model.addAttribute("serviceNames", this.serviceResource.getServices());
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("edit")
    public String edit(final Model model,
                       @RequestParam(name = "id") final Long id) {
        final RouteGatewayDto routeGateway = this.routeGatewayService.getById(id);
        routeGateway.setPredicates(CommonTool.formatTextarea(routeGateway.getPredicates()));
        routeGateway.setFilters(CommonTool.formatTextarea(routeGateway.getFilters()));
        routeGateway.setMetadata(CommonTool.formatTextarea(routeGateway.getMetadata()));

        model.addAttribute("gatewayNames", this.serviceResource.getGatewayList(RouteGatewayService.GATEWAY_TYPE));
        model.addAttribute("serviceNames", this.serviceResource.getServices());
        model.addAttribute("route", routeGateway);
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("do-list")
    @ResponseBody
    public Result<List<RouteGatewayDto>> doList(@RequestParam(value = "page") final Integer pageNum,
                                                @RequestParam(value = "limit") final Integer pageSize,
                                                @RequestParam(value = "description", required = false) final String description) {
        final IPage<RouteGatewayDto> page = this.routeGatewayService.page(description, pageNum, pageSize);
        for (final RouteGatewayDto record : page.getRecords()) {
            record.setMetadata(record.getMetadata().replaceAll(PlatformConstant.ROW_SEPARATOR, ", "));
        }
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("do-list-working")
    @ResponseBody
    public Result<List<GatewayRouteVo>> doListWorking(@RequestParam(value = "gatewayName", required = false) final String gatewayName) {
        if (ObjectUtils.isEmpty(gatewayName)) {
            return Result.ok();
        }

        final List<GatewayRouteVo> result = new ArrayList<>();
        final List<ResultEntity> resultEntityList = this.routeResource.viewAllRoute(RouteGatewayService.GATEWAY_TYPE, gatewayName);
        for (final ResultEntity resultEntity : resultEntityList) {
            final GatewayRouteVo gatewayRouteVo = new GatewayRouteVo();
            gatewayRouteVo.setHost(resultEntity.getHost());
            gatewayRouteVo.setPort(String.valueOf(resultEntity.getPort()));
            gatewayRouteVo.setRoutes(JsonUtil.fromJson(resultEntity.getResult(), new TypeReference<List<RouteGatewayPo>>() {
            }));
            result.add(gatewayRouteVo);
        }
        return Result.ok(result);
    }

    @PostMapping("do-list-gateway-names")
    @ResponseBody
    public Result<List<String>> doListGatewayNames(@RequestParam(value = "gatewayName", required = false) final String gatewayName) {
        return Result.ok(this.serviceResource.getGatewayList(RouteGatewayService.GATEWAY_TYPE));
    }

    @PostMapping("do-add")
    @ResponseBody
    public Result<?> doAdd(RouteGatewayDto routeGateway) {
        this.routeGatewayService.insert(routeGateway);
        return Result.ok();
    }

    @PostMapping("do-edit")
    @ResponseBody
    public Result<?> doEdit(RouteGatewayDto routeGateway) {
        this.routeGatewayService.update(routeGateway);
        return Result.ok();
    }

    @PostMapping("do-enable")
    @ResponseBody
    public Result<?> doEnable(@RequestParam(value = "id") final Long id) {
        this.routeGatewayService.enable(id, true);
        return Result.ok();
    }

    @PostMapping("do-disable")
    @ResponseBody
    public Result<?> doDisable(@RequestParam(value = "id") final Long id) {
        this.routeGatewayService.enable(id, false);
        return Result.ok();
    }

    @PostMapping("do-delete")
    @ResponseBody
    public Result<?> doDelete(@RequestParam(value = "ids") final String ids) {
        final List<Long> idList = CommonTool.parseList(ids, ",", Long.class);
        this.routeGatewayService.logicDelete(new HashSet<>(idList));
        return Result.ok();
    }

    @PostMapping("do-publish")
    @ResponseBody
    public Result<?> doPublish() throws Exception {
        this.routeGatewayService.publish();
        return Result.ok();
    }

}