var d = [
    {
        "module": "模块A",
        "children": [
            {
                "name": "一级目录 1",
                "icon": "abc",
                "description": "描述信息",
                "children": [
                    {
                        "name": "二级目录 1-1",
                        "children": [
                            {
                                "name": "三级目录 1-1-1",
                                "path": "/aaa/bbb/cc",
                                "privs": [
                                    {
                                        "label": "按钮权限A",
                                        "code": "dddd"
                                    },
                                    {
                                        "label": "按钮权限B",
                                        "code": "ff",
                                        "description": "这里是按钮的描述信息"
                                    },
                                    {
                                        "label": "按钮权限C",
                                        "code": "dd"
                                    }]

                            },
                            {
                                "name": "三级目录 1-1-2",
                                "path": "/aaa/bbb/cc",
                                "privs": [
                                    {
                                        "label": "按钮权限D",
                                        "code": "dddd"
                                    },
                                    {
                                        "label": "按钮权限E",
                                        "code": "ff"
                                    },
                                    {
                                        "label": "按钮权限F",
                                        "code": "dd"
                                    }]

                            }]

                    }]
            },
            {
                "name": "一级目录 2",
                "icon": "abc",
                "description": "描述信息",
                "children": [
                    {
                        "name": "二级目录 2-1",
                        "path": "/page/2/1",
                        "privs": [
                            {
                                "label": "按钮权限A",
                                "code": "dddd"
                            },
                            {
                                "label": "按钮权限B",
                                "code": "ff",
                                "description": "这里是按钮的描述信息"
                            },
                            {
                                "label": "按钮权限C",
                                "code": "dd"
                            }]

                    }]
            }]
    },
    {
        "module": "模块B",
        "children": [
            {
                "name": "一级目录 D",
                "path": "/aaa/bbb/cc"
            }]
    }]
