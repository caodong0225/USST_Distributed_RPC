'use client';

import ReactMarkdown from 'react-markdown';
import { Tag, Select, Input, Button, Form, Skeleton, Table, Pagination, message } from "antd";
import remarkGfm from 'remark-gfm';
import { useState, useEffect, createContext, useContext } from 'react';
import * as jobsApi from '../../api/jobs';
import TextArea from 'antd/lib/input/TextArea';

// 创建上下文用于共享刷新函数
const JobsContext = createContext({
  refreshJobs: () => {},
});

const WelcomeContent = () => {
  const [isWelcomeLoading, setIsWelcomeLoading] = useState(true);
  const [welcomeData, setWelcomeData] = useState(null);
  


  useEffect(() => {
    // 模拟加载数据
    setTimeout(() => {
      setIsWelcomeLoading(false);
    }, 500);
  }, []);

  if (isWelcomeLoading) {
      return (
          <div className="bg-white p-4 shadow-md rounded w-full">
              <Skeleton active />
          </div>
      );
  }

  const defaultContent = `
本平台是一个招聘平台，用来整理招聘公司信息。

平台手搓RPC架构，注册中心采用Tomcat+Redis的结构，客户端采用前后端分离的架构，前端使用NextJS+Tailwindcss的架构，后端使用Springboot，通过Feign的方式访问远程服务器的方法，服务端采用Springboot+MybatisPlus的架构，由JWT的方式实现跨平台数据访问。
  `;

  return (
      <div className="bg-white p-4 shadow-md rounded w-full">
          <h2 className="text-2xl font-bold">{welcomeData?.title || "欢迎来到求职网站"}</h2>
          <div className="mt-5 markdown-content">
              <ReactMarkdown 
                  remarkPlugins={[remarkGfm]}
                  components={{
                      // 自定义链接在新标签页打开
                      a: ({node, ...props}) => (
                          <a target="_blank" rel="noopener noreferrer" {...props} />
                      ),
                      // 保持段落的样式
                      p: ({node, ...props}) => (
                          <p className="mb-4" {...props} />
                      ),
                      // 保持标题的样式
                      h1: ({node, ...props}) => (
                          <h1 className="text-2xl font-bold mb-4" {...props} />
                      ),
                      h2: ({node, ...props}) => (
                          <h2 className="text-xl font-bold mb-3" {...props} />
                      ),
                      h3: ({node, ...props}) => (
                          <h3 className="text-lg font-bold mb-2" {...props} />
                      ),
                      // 保持列表的样式
                      ul: ({node, ...props}) => (
                          <ul className="list-disc list-inside mb-4" {...props} />
                      ),
                      ol: ({node, ...props}) => (
                          <ol className="list-decimal list-inside mb-4" {...props} />
                      ),
                      // 保持代码块的样式
                      code: ({node, inline, ...props}) => (
                          inline ? 
                          <code className="bg-gray-100 px-1 rounded" {...props} /> :
                          <code className="block bg-gray-100 p-4 rounded mb-4" {...props} />
                      )
                  }}
              >
                  {welcomeData?.contentExt?.contentMarkdown || defaultContent}
              </ReactMarkdown>
          </div>
      </div>
  );
};

const JobList = () => {
  const [loading, setLoading] = useState(false);
  const [jobList, setJobList] = useState([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  // 使用上下文中的刷新函数
  const { refreshJobs } = useContext(JobsContext);

  const fetchJobs = async (params = {}) => {
    setLoading(true);
    try {
      const newParams = {
        current: params.current || pagination.current,
        pageSize: params.pageSize || pagination.pageSize,
      };
      
      // 尝试从API获取数据，如果失败则使用mock数据
      let result;
      try {
        result = await jobsApi.getJobs(newParams);
      } catch (error) {
        console.log('使用mock数据', error);
        // 模拟网络延迟
        await new Promise(resolve => setTimeout(resolve, 500));
        result = { list: [], total: 0 };
      }
      
      setJobList(result?.list || []);
      setPagination({
        ...pagination,
        current: params.current || pagination.current,
        total: result?.total || 0,
      });
    } catch (error) {
      console.error('获取职位列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchJobs();
    // 为上下文提供刷新函数
    refreshJobs.current = fetchJobs;
  }, []);

  const handlePageChange = (page, pageSize) => {
    fetchJobs({
      current: page,
      pageSize: pageSize,
    });
  };

  // 格式化日期时间
  const formatDateTime = (dateTimeStr) => {
    if (!dateTimeStr) return '';
    const date = new Date(dateTimeStr);
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
  };

  return (
    <div className="bg-white p-4 shadow-md rounded w-full">
      <h2 className="text-xl font-bold mb-4">最新职位</h2>
      
      {loading ? (
        <Skeleton active />
      ) : (
        <>
          <div className="mb-4">
            {jobList.length > 0 ? (
              jobList.map((job) => (
                <div key={job.id} className="border-b pb-3 mb-3">
                  <div className="flex justify-between items-start">
                    <h3 className="text-lg font-medium">{job.name}</h3>
                    <span className="text-blue-500">{job.salary}</span>
                  </div>
                  <div className="text-gray-500 text-sm mt-1">{job.user?.username}</div>
                  <div className="mt-2 prose prose-sm max-w-none">
                    <ReactMarkdown 
                      remarkPlugins={[remarkGfm]}
                      components={{
                        // 自定义链接在新标签页打开
                        a: ({node, ...props}) => (
                          <a target="_blank" rel="noopener noreferrer" {...props} className="text-blue-500 hover:underline" />
                        ),
                        // 保持段落的样式
                        p: ({node, ...props}) => (
                          <p className="mb-2" {...props} />
                        ),
                        // 保持列表的样式
                        ul: ({node, ...props}) => (
                          <ul className="list-disc list-inside mb-2" {...props} />
                        ),
                        ol: ({node, ...props}) => (
                          <ol className="list-decimal list-inside mb-2" {...props} />
                        ),
                        // 保持代码块的样式
                        code: ({node, inline, ...props}) => (
                          inline ? 
                          <code className="bg-gray-100 px-1 rounded" {...props} /> :
                          <code className="block bg-gray-100 p-2 rounded mb-2 text-xs" {...props} />
                        )
                      }}
                    >
                      {job.description}
                    </ReactMarkdown>
                  </div>
                  <div className="mt-2 flex justify-between items-center">
                    <div className="text-xs text-gray-400">
                      发布时间: {formatDateTime(job.createdAt)}
                    </div>
                    {job.status === 'finish' && (
                      <div className="text-xs text-gray-400">停止招聘</div>
                    )}
                    {job.status === 'ongoing' && (
                      <div className="text-xs text-orange-400">招聘中</div>
                    )}
                  </div>
                </div>
              ))
            ) : (
              <div className="text-center py-4 text-gray-500">暂无职位信息</div>
            )}
          </div>
          
          <div className="flex justify-center mt-6">
            <Pagination
              current={pagination.current}
              pageSize={pagination.pageSize}
              total={pagination.total}
              onChange={handlePageChange}
              showSizeChanger
              showQuickJumper
              showTotal={(total) => `共 ${total} 条记录`}
            />
          </div>
        </>
      )}
    </div>
  );
};

const CreateJobForm = () => {
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const [markdownPreview, setMarkdownPreview] = useState('');
  const [showPreview, setShowPreview] = useState(false);
  
  // 使用JobsContext获取刷新函数
  const { refreshJobs } = useContext(JobsContext);

  const onFinish = async (values) => {
    setSubmitting(true);
    try {
      const jobData = {
        ...values,
      };

      // 调用API创建工作
      await jobsApi.addJob(jobData);
      messageApi.success('工作创建成功！');
      form.resetFields();
      // 不要全部刷新，只要刷新jobsList
      refreshJobs.current(); // 通过引用调用刷新函数
      setMarkdownPreview('');
    } catch (error) {
      console.error('创建工作失败:', error);
      messageApi.error({
        content: `创建工作失败: ${error?.message || '未知错误'}`,
      });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDescriptionChange = (e) => {
    setMarkdownPreview(e.target.value);
  };

  return (
    <div className="bg-white p-4 shadow-md rounded w-full">
      {contextHolder}
      <h2 className="text-xl font-bold mb-4">创建新职位</h2>
      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
        initialValues={{ status: 'pending' }}
      >
        <Form.Item
          name="name"
          label="职位名称"
          rules={[{ required: true, message: '请输入职位名称' }]}
        >
          <Input placeholder="例如：Java高级开发工程师" />
        </Form.Item>

        <Form.Item
          name="salary"
          label="薪资范围"
          rules={[
            { required: true, message: '请输入薪资范围' },
            { pattern: /^(\d+)(~\d+)?$/, message: '薪资必须是"10000~20000"格式，用~分割' }
          ]}
        >
          <Input placeholder="例如：10000~20000" />
        </Form.Item>

        <Form.Item
          name="description"
          label={
            <div className="flex justify-between w-full">
              <span>职位描述</span>
              <Button 
                type="link" 
                size="small" 
                onClick={() => setShowPreview(!showPreview)}
                className="p-0"
              >
                {showPreview ? '编辑模式' : '预览模式'}
              </Button>
            </div>
          }
          rules={[{ required: true, message: '请输入职位描述' }]}
        >
          {!showPreview ? (
            <TextArea 
              rows={8} 
              placeholder="请详细描述职位要求和职责...支持Markdown格式
## 岗位职责
1. 负责XXX系统开发和维护
2. 参与XXX项目设计和实现

## 岗位要求
- 熟悉XXX技术栈
- 具有XXX年相关工作经验"
              onChange={handleDescriptionChange}
            />
          ) : (
            <div className="border p-4 rounded min-h-[200px] prose prose-sm max-w-none overflow-auto">
              <ReactMarkdown
                remarkPlugins={[remarkGfm]}
                components={{
                  a: ({node, ...props}) => (
                    <a target="_blank" rel="noopener noreferrer" {...props} className="text-blue-500 hover:underline" />
                  ),
                  p: ({node, ...props}) => (
                    <p className="mb-2" {...props} />
                  ),
                  ul: ({node, ...props}) => (
                    <ul className="list-disc list-inside mb-2" {...props} />
                  ),
                  ol: ({node, ...props}) => (
                    <ol className="list-decimal list-inside mb-2" {...props} />
                  ),
                  code: ({node, inline, ...props}) => (
                    inline ? 
                    <code className="bg-gray-100 px-1 rounded" {...props} /> :
                    <code className="block bg-gray-100 p-2 rounded mb-2 text-xs" {...props} />
                  )
                }}
              >
                {markdownPreview}
              </ReactMarkdown>
            </div>
          )}
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" loading={submitting} className="bg-blue-500">
            提交
          </Button>
        </Form.Item>
      </Form>
    </div>
  );
};

export default function Home() {
  // 创建一个ref用于存储刷新函数
  const refreshJobsRef = { current: () => {} };

  return (
    <JobsContext.Provider value={{ refreshJobs: refreshJobsRef }}>
      <div className="min-h-screen bg-cover bg-center bg-no-repeat" style={{ backgroundImage: "url('/bg.jpg')" }}>
        <div className="flex max-w-screen-xl mx-auto flex-wrap pb-5">
          <div className="pt-5 md:w-1/2 pb-0 md:pr-0 flex justify-start p-5 lg:w-2/3 flex-col gap-5">
            <JobList />
            <CreateJobForm />
          </div>
          <div className="w-full md:w-1/2 p-5 lg:w-1/3 gap-5 flex flex-col">
            <WelcomeContent />
          </div>
        </div>
      </div>
    </JobsContext.Provider>
  );
}
  