import packageJson from '../../package.json';

export function getSiteConfig() {
    return {
        siteName: '招聘网站',
        copyRightFooterName: `${new Date().getFullYear()} 曹东 分布式计算作业 V${packageJson.version}`,
    }
}
